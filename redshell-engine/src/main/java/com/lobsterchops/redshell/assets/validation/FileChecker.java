package com.lobsterchops.redshell.assets.validation;

/** Answers whether an asset file exists. Abstracted so validation can run without a file system. */
public interface FileChecker {

    boolean exists(String path);
}