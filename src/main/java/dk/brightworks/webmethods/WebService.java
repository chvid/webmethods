package dk.brightworks.webmethods;

import dk.brightworks.autowirer.wire.Service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface WebService {
    String value();
}
