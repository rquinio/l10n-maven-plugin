report = new File(basedir, "moduleA/target/site/l10n-report.html");
assert report.size() > 0;
report = new File(basedir, "moduleB/target/site/l10n-report.html");
assert report.size() > 0;
report = new File(basedir, "target/site/index.html");
assert report.size() > 0;