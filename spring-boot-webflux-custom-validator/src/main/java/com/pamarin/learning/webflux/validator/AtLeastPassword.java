/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.learning.webflux.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.joining;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import org.springframework.stereotype.Component;
import static org.springframework.util.StringUtils.hasText;

/**
 *
 * @author jitta
 */
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = AtLeastPassword.Validator.class)
public @interface AtLeastPassword {

    String message() default "at least one lower case letter, one upper case letter, one special character and one numeric digit";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Component
    public static class Validator implements ConstraintValidator<AtLeastPassword, String> {

        //https://www.owasp.org/index.php/Password_special_characters
        private static final List<String> OWASP_SPECIAL_CHARACTERS = Arrays.asList(
                "s",
                "!",
                "\"",
                "#",
                "$",
                "%",
                "&",
                "'",
                "(",
                ")",
                "*",
                "+",
                ",",
                "-",
                ".",
                "/",
                ":",
                ";",
                "<",
                "=",
                ">",
                "?",
                "@",
                "[",
                "\\",
                "]",
                "^",
                "_",
                "`",
                "{",
                "|",
                "}",
                "~"
        );

        private final String specialCharactersRegExp;

        public Validator() {
            this.specialCharactersRegExp = getSpecialCharacterRegExp();

        }

        @Override
        public void initialize(AtLeastPassword a) {
            //Not used
        }

        private boolean matches(String str, String regExp) {
            Pattern pattern = Pattern.compile(".*" + regExp + ".*");
            return pattern.matcher(str).matches();
        }

        @Override
        public boolean isValid(String password, ConstraintValidatorContext context) {
            if (!hasText(password)) {
                return true;
            }

            if (!matches(password, "[a-zA-Z\\d]+")) {
                return false;
            }

            return matches(password, specialCharactersRegExp);
        }

        public boolean isValid(String password) {
            return isValid(password, null);
        }

        public static final List<String> getSpecialCharacters() {
            return Collections.unmodifiableList(OWASP_SPECIAL_CHARACTERS);
        }

        public static final String getSpecialCharacterRegExp() {
            return "[" + OWASP_SPECIAL_CHARACTERS.stream()
                    .map(ch -> "\\" + ch)
                    .collect(joining("")) + "]+";
        }
    }

}
