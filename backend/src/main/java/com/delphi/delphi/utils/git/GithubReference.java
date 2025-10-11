package com.delphi.delphi.utils.git;

public class GithubReference {
    String ref;

    public GithubReference() {}

    public GithubReference(String ref) {
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String toString() {
        return String.format("""
                ref: {ref}
                """, ref);
    }

}
