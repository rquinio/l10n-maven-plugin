log = new File(basedir, "build.log");
assert log.getText().contains("[ERROR] Validation has failed");
assert log.getText().contains("[INFO] Ignoring failure as ignoreFailure is true.");