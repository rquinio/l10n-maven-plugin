log = new File(basedir, "build.log");
assert log.getText().contains("[ERROR] Validation has failed with 1 errors");
assert log.getText().contains("[ERROR] Validation has failed with 2 errors");
