package org.teacon.slides.renderer;

import com.google.common.base.Objects;

public class SlideStateProperties {
    private final String location;
    private final boolean enableLod;

    public SlideStateProperties(String location, boolean enableLod) {
        this.location = location;
        this.enableLod = enableLod;
    }

    public boolean getEnableLod() {
        return enableLod;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) return true;
        if(!(o instanceof SlideStateProperties)) return false;

        return (this.location.equals(((SlideStateProperties) o).location) && this.enableLod == ((SlideStateProperties) o).enableLod);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.enableLod, this.location);
    }
}
