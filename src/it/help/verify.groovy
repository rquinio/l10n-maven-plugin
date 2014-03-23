log = new File(basedir, "build.log")
assert log.getText().contains("This plugin has 3 goals");