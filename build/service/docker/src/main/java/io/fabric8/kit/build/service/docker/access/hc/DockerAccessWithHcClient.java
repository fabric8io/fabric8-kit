package io.fabric8.kit.build.service.docker.access.hc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fabric8.kit.build.service.docker.access.BuildOptions;
import io.fabric8.kit.build.service.docker.access.DockerAccess;
import io.fabric8.kit.build.service.docker.access.DockerAccessException;
import io.fabric8.kit.build.service.docker.access.UrlBuilder;
import io.fabric8.kit.build.service.docker.access.chunked.BuildJsonResponseHandler;
import io.fabric8.kit.build.service.docker.access.chunked.EntityStreamReaderUtil;
import io.fabric8.kit.build.service.docker.access.chunked.PullOrPushResponseJsonHandler;
import io.fabric8.kit.build.service.docker.access.hc.ApacheHttpClientDelegate.BodyAndStatusResponseHandler;
import io.fabric8.kit.build.service.docker.access.hc.ApacheHttpClientDelegate.HttpBodyAndStatus;
import io.fabric8.kit.build.service.docker.access.hc.http.HttpClientBuilder;
import io.fabric8.kit.build.service.docker.access.hc.unix.UnixSocketClientBuilder;
import io.fabric8.kit.build.service.docker.access.hc.util.ClientBuilder;
import io.fabric8.kit.build.service.docker.access.hc.win.NamedPipeClientBuilder;
import io.fabric8.kit.common.JsonFactory;
import io.fabric8.kit.common.KitLogger;
import io.fabric8.kit.config.image.ImageName;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Implementation using <a href="http://hc.apache.org/">Apache HttpComponents</a>
 * for remotely accessing the docker host.
 * <p/>
 * The design goal here is to provide only the functionality required for this plugin in order to
 * make it as robust as possible against docker API changes (which happen quite frequently). That's
 * also the reason, why no framework like JAX-RS or docker-java is used so that the dependencies are
 * kept low.
 * <p/>
 * Of course, it's a bit more manual work, but it's worth the effort
 * (as long as the Docker API functionality required is not too much).
 *
 * @author roland
 * @since 26.03.14
 */
public class DockerAccessWithHcClient implements DockerAccess {

    // Base URL which is given through when using UnixSocket communication but is not really used
    private static final String UNIX_URL = "unix://127.0.0.1:1/";

    // Base URL which is given through when using NamedPipe communication but is not really used
    private static final String NPIPE_URL = "npipe://127.0.0.1:1/";

    // Logging
    private final KitLogger log;

    private final ApacheHttpClientDelegate delegate;
    private final UrlBuilder urlBuilder;

    /**
     * Create a new access for the given URL
     *
     * @param baseUrl  base URL for accessing the docker Daemon
     * @param certPath used to build up a keystore with the given keys and certificates found in this
     *                 directory
     * @param maxConnections maximum parallel connections allowed to docker daemon (if a pool is used)
     * @param log      a log handler for printing out logging information
     * @paran usePool  whether to use a connection bool or not
     */
    public DockerAccessWithHcClient(String apiVersion,
                                    String baseUrl,
                                    String certPath,
                                    int maxConnections,
                                    KitLogger log) throws IOException {
        this.log = log;
        URI uri = URI.create(baseUrl);
        if (uri.getScheme() == null) {
            throw new IllegalArgumentException("The docker access url '" + baseUrl + "' must contain a schema tcp://, unix:// or npipe://");
        }
        if (uri.getScheme().equalsIgnoreCase("unix")) {
            this.delegate = createHttpClient(new UnixSocketClientBuilder(uri.getPath(), maxConnections, log));
            this.urlBuilder = new UrlBuilder(UNIX_URL, apiVersion);
        } else if (uri.getScheme().equalsIgnoreCase("npipe")) {
        	this.delegate = createHttpClient(new NamedPipeClientBuilder(uri.getPath(), maxConnections, log), false);
            this.urlBuilder = new UrlBuilder(NPIPE_URL, apiVersion);
        } else {
            this.delegate = createHttpClient(new HttpClientBuilder(isSSL(baseUrl) ? certPath : null, maxConnections));
            this.urlBuilder = new UrlBuilder(baseUrl, apiVersion);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getServerApiVersion() throws DockerAccessException {
        try {
            String url = urlBuilder.version();
            String response = delegate.get(url, 200);
            JsonObject info = JsonFactory.newJsonObject(response);
            return info.get("ApiVersion").getAsString();
        } catch (Exception e) {
            throw new DockerAccessException(e, "Cannot extract API version from server %s", urlBuilder.getBaseUrl());
        }
    }


    @Override
    public void buildImage(String image, File dockerArchive, BuildOptions options) throws DockerAccessException {
        try {
            String url = urlBuilder.buildImage(image, options);
            delegate.post(url, dockerArchive, createBuildResponseHandler(), HTTP_OK);
        } catch (IOException e) {
            throw new DockerAccessException(e, "Unable to build image [%s]", image);
        }
    }


    @Override
    public boolean hasImage(String name) throws DockerAccessException {
        String url = urlBuilder.inspectImage(name);
        try {
            return delegate.get(url, new ApacheHttpClientDelegate.StatusCodeResponseHandler(), HTTP_OK, HTTP_NOT_FOUND) == HTTP_OK;
        } catch (IOException e) {
            throw new DockerAccessException(e, "Unable to check image [%s]", name);
        }
    }

    @Override
    public String getImageId(String name) throws DockerAccessException {
        HttpBodyAndStatus response = inspectImage(name);
        if (response.getStatusCode() == HTTP_NOT_FOUND) {
            return null;
        }
        JsonObject imageDetails = JsonFactory.newJsonObject(response.getBody());
        return imageDetails.get("Id").getAsString().substring(0, 12);
    }

    private HttpBodyAndStatus inspectImage(String name) throws DockerAccessException {
        String url = urlBuilder.inspectImage(name);
        try {
            return delegate.get(url, new BodyAndStatusResponseHandler(), HTTP_OK, HTTP_NOT_FOUND);
        } catch (IOException e) {
            throw new DockerAccessException(e, "Unable to inspect image [%s]", name);
        }
    }

    @Override
    public void loadImage(String image, File tarArchive) throws DockerAccessException {
        String url = urlBuilder.loadImage();

        try {
            delegate.post(url, tarArchive, new BodyAndStatusResponseHandler(), HTTP_OK);
        } catch (IOException e) {
            throw new DockerAccessException(e, "Unable to load %s", tarArchive);
        }
    }

    @Override
    public void pullImage(String image, String authHeader, String registry)
            throws DockerAccessException {
        ImageName name = new ImageName(image);
        String pullUrl = urlBuilder.pullImage(name, registry);

        try {
            delegate.post(pullUrl, null, createAuthHeader(authHeader), createPullOrPushResponseHandler(), HTTP_OK);
        } catch (IOException e) {
            throw new DockerAccessException(e, "Unable to pull '%s'%s", image, (registry != null) ? " from registry '" + registry + "'" : "");
        }
    }

    @Override
    public void pushImage(String image, String authHeader, String registry, int retries)
            throws DockerAccessException {
        ImageName name = new ImageName(image);
        String pushUrl = urlBuilder.pushImage(name, registry);
        String temporaryImage = tagTemporaryImage(name, registry);
        DockerAccessException dae = null;
        try {
            doPushImage(pushUrl, createAuthHeader(authHeader), createPullOrPushResponseHandler(), HTTP_OK, retries);
        } catch (IOException e) {
            dae = new DockerAccessException(e, "Unable to push '%s'%s", image, (registry != null) ? " from registry '" + registry + "'" : "");
            throw dae;
        } finally {
            if (temporaryImage != null) {
                if (!removeImage(temporaryImage, true)) {
                    if (dae == null) {
                        throw new DockerAccessException("Image %s could be pushed, but the temporary tag could not be removed", temporaryImage);
                    } else {
                        throw new DockerAccessException(dae.getCause(), dae.getMessage() + " and also temporary tag [%s] could not be removed, too.", temporaryImage);
                    }
                }
            }
        }
    }

    @Override
    public void saveImage(String image, String filename) throws DockerAccessException {
        ImageName name = new ImageName(image);
        String url = urlBuilder.getImage(name);
        try {
            delegate.get(url, getImageResponseHandler(filename), HTTP_OK);
        } catch (IOException e) {
            throw new DockerAccessException(e, "Unable to save '%s' to '%s'", image, filename);
        }

    }

    private ResponseHandler<Object> getImageResponseHandler(final String filename) throws FileNotFoundException {
        return response -> {
            OutputStream fout = new FileOutputStream(filename);
            OutputStream out;
            if (filename.endsWith(".tar.gz") || filename.endsWith(".tgz")) {
                out = new GZIPOutputStream(fout);
            } else if (filename.endsWith(".tar.bz") || filename.endsWith(".tar.bzip2") || filename.endsWith(".tar.bz2")) {
                out = new BZip2CompressorOutputStream(fout);
            } else {
                out = fout;
            }

            try {
                try (InputStream stream = response.getEntity().getContent()) {
                    IOUtils.copy(stream, out);
                }
            } finally {
                out.close();
            }
            return null;
        };
    }

    @Override
    public void tag(String sourceImage, String targetImage, boolean force)
            throws DockerAccessException {
        ImageName source = new ImageName(sourceImage);
        ImageName target = new ImageName(targetImage);
        try {
            String url = urlBuilder.tagImage(source, target, force);
            delegate.post(url, HTTP_CREATED);
        } catch (IOException e) {
            throw new DockerAccessException(e, "Unable to add tag [%s] to image [%s]", targetImage,
                    sourceImage, e);
        }
    }

    @Override
    public boolean removeImage(String image, boolean... forceOpt) throws DockerAccessException {
        boolean force = forceOpt != null && forceOpt.length > 0 && forceOpt[0];
        try {
            String url = urlBuilder.deleteImage(image, force);
            HttpBodyAndStatus response = delegate.delete(url, new BodyAndStatusResponseHandler(), HTTP_OK, HTTP_NOT_FOUND);
            if (log.isDebugEnabled()) {
                logRemoveResponse(JsonFactory.newJsonArray(response.getBody()));
            }

            return response.getStatusCode() == HTTP_OK;
        } catch (IOException e) {
            throw new DockerAccessException(e, "Unable to remove image [%s]", image);
        }
    }

    protected ApacheHttpClientDelegate createHttpClient(ClientBuilder builder) throws IOException {
    	return createHttpClient(builder, true);
    }

    private ApacheHttpClientDelegate createHttpClient(ClientBuilder builder, boolean pooled) throws IOException {
        return new ApacheHttpClientDelegate(builder, pooled);
    }

    // visible for testing?
    private HcChunkedResponseHandlerWrapper createBuildResponseHandler() {
        return new HcChunkedResponseHandlerWrapper(new BuildJsonResponseHandler(log));
    }

    // visible for testing?
    private HcChunkedResponseHandlerWrapper createPullOrPushResponseHandler() {
        return new HcChunkedResponseHandlerWrapper(new PullOrPushResponseJsonHandler(log));
    }

    private Map<String, String> createAuthHeader(String authConfig) {
        return Collections.singletonMap("X-Registry-Auth", authConfig);
    }

    private boolean isRetryableErrorCode(int errorCode) {
        // there eventually could be more then one of this
        return errorCode == HTTP_INTERNAL_ERROR;
    }

    private void doPushImage(String url, Map<String, String> header, HcChunkedResponseHandlerWrapper handler, int status,
                             int retries) throws IOException {
        // 0: The original attemp, 1..retry: possible retries.
        for (int i = 0; i <= retries; i++) {
            try {
                delegate.post(url, null, header, handler, HTTP_OK);
                return;
            } catch (HttpResponseException e) {
                if (isRetryableErrorCode(e.getStatusCode()) && i != retries) {
                    log.warn("failed to push image to [{}], retrying...", url);
                } else {
                    throw e;
                }
            }
        }
    }

    private String tagTemporaryImage(ImageName name, String registry) throws DockerAccessException {
        String targetImage = name.getFullName(registry);
        if (!name.hasRegistry() && registry != null) {
            if (hasImage(targetImage)) {
                throw new DockerAccessException(
                    String.format("Cannot temporarily tag %s with %s because target image already exists. " +
                                  "Please remove this and retry.",
                                  name.getFullName(), targetImage));
            }
            tag(name.getFullName(), targetImage, false);
            return targetImage;
        }
        return null;
    }

    // ===========================================================================================================

    private void logWarnings(JsonObject body) {
        if (body.has("Warnings")) {
            JsonElement warningsObj = body.get("Warnings");
            if (!warningsObj.isJsonNull()) {
                JsonArray warnings = (JsonArray) warningsObj;
                for (int i = 0; i < warnings.size(); i++) {
                    log.warn(warnings.get(i).getAsString());
                }
            }
        }
    }

    // Callback for processing response chunks
    private void logRemoveResponse(JsonArray logElements) {
        for (int i = 0; i < logElements.size(); i++) {
            JsonObject entry = logElements.get(i).getAsJsonObject();
            for (Object key : entry.keySet()) {
                log.debug("%s: %s", key, entry.get(key.toString()));
            }
        }
    }

    private boolean isSSL(String url) {
        return url != null && url.toLowerCase().startsWith("https");
    }

    // Preparation for performing requests
    private static class HcChunkedResponseHandlerWrapper implements ResponseHandler<Object> {

        private EntityStreamReaderUtil.JsonEntityResponseHandler handler;

        HcChunkedResponseHandlerWrapper(EntityStreamReaderUtil.JsonEntityResponseHandler handler) {
            this.handler = handler;
        }

        @Override
        public Object handleResponse(HttpResponse response) throws IOException {
            try (InputStream stream = response.getEntity().getContent()) {
                // Parse text as json
                EntityStreamReaderUtil.processJsonStream(handler, stream);
            }
            return null;
        }
    }
}
