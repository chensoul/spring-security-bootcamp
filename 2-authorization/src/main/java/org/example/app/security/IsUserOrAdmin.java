package org.example.app.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Retention(RetentionPolicy.RUNTIME)
public @interface IsUserOrAdmin {
}
