package io.fabric8.kit.config.image;

import java.io.Serializable;

import io.fabric8.kit.config.image.util.DeepCopy;

/**
 * Configuration for watching on image changes
 */
public class WatchImageConfiguration implements Serializable {


    private int interval = 5000; // default


    private WatchMode mode;


    private String postGoal;


    private String postExec;

    public WatchImageConfiguration() {};

    public int getInterval() {
        return interval;
    }

    public WatchMode getMode() {
        return mode;
    }

    public String getPostGoal() {
        return postGoal;
    }

    public String getPostExec() {
        return postExec;
    }

    public static class Builder {

        private final WatchImageConfiguration c;

        public Builder() {
            this(null);
        }

        public Builder(WatchImageConfiguration that) {
            if (that == null) {
                this.c = new WatchImageConfiguration();
            } else {
                this.c = DeepCopy.copy(that);
            }
        }

        public Builder interval(int interval) {
            c.interval = interval;
            return this;
        }

        public Builder mode(String mode) {
            if (mode != null) {
                c.mode = WatchMode.valueOf(mode.toLowerCase());
            }
            return this;
        }

        public Builder postGoal(String goal) {
            c.postGoal = goal;
            return this;
        }

        public WatchImageConfiguration build() {
            return c;
        }
    }


}
