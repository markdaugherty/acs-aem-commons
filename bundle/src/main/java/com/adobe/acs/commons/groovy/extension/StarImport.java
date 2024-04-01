package com.adobe.acs.commons.groovy.extension;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

public class StarImport implements Comparable<StarImport> {

    /** Star import package name. */
    private final String packageName;

    /** Optional link to documentation (Javadoc/Groovydoc). */
    private final String link;

    /**
     * Create a new star import for the given package name.
     *
     * @param packageName package name
     */
    public StarImport(final String packageName) {
        this.packageName = packageName;
        this.link = null;
    }

    /**
     * Create a new star import for the given package name and documentation link.
     *
     * @param packageName package name
     * @param link link to documentation URL
     */
    public StarImport(final String packageName, final String link) {
        this.packageName = packageName;
        this.link = link;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getLink() {
        return link;
    }

    @Override
    public int compareTo(@NotNull final StarImport starImport) {
        return this.getPackageName().compareTo(starImport.getPackageName());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("packageName", packageName)
            .append("link", link)
            .toString();
    }
}
