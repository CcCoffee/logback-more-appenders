open module org.slf4j {
  requires transitive slf4j.api;
  requires static logback.core;
  requires static logback.classic;
  requires static logback.access;
  requires static fluent.logger;
}
