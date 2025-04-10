package com.memozy.memozy_back.global.util.validation;

import static com.memozy.memozy_back.domain.user.domain.Password.PASSWORD_REGXP;
import static com.memozy.memozy_back.global.util.CommonConstant.MAX_TEXT_LENGTH;
import static io.micrometer.common.util.StringUtils.isBlank;
import static io.micrometer.common.util.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//public final class ValidationUtils {
//
//    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGXP);
//
//    private static final Pattern EMAIL_PATTERN = Pattern.compile(
//            "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+[.][0-9A-Za-z]+$");
//
//    private static final Pattern URI_PATTERN = Pattern.compile(URL_REGEXP);
//
//
//    public static void validateEmail(String input, String message) {
//        pattern(input, EMAIL_PATTERN, message);
//    }
//
//    public static void validateURI(String input, String message) {
//        pattern(input, URI_PATTERN, message);
//    }
//
//    public static void validatePassword(String input, String message) {
//        pattern(input, PASSWORD_PATTERN, message);
//    }
//
//    public static void validateMaxLength(String input, int maxLength, String message) {
//        Assert.isTrue(input.length() <= maxLength, message);
//    }
//
//    public static void validateTextLength(String input, String message) {
//        Assert.isTrue(input.length() <= MAX_TEXT_LENGTH, message);
//    }
//
//    public static void validateNotBlank(String input, String message) {
//        Assert.isTrue(isNotBlank(input), message);
//    }
//
//    public static void validateNotNull(Object input, String message) {
//        Assert.notNull(input, message);
//    }
//
//    public static void validateBlank(String input, String message) {
//        Assert.isTrue(isBlank(input), message);
//    }
//
//    public static <T> void validateNotNullAndEmpty(Collection<T> input, String message) {
//        Assert.isTrue(isNotNullOrEmpty(input), message);
//    }
//
//    public static <T, U> void validateInclude(
//            List<T> values, BiPredicate<T, U> condition, U requiredValue, String errorMessage
//    ) {
//        values.stream().filter(value -> condition.test(value, requiredValue))
//                .findAny()
//                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
//    }
//
//    public static <T extends Collection<U>, U> void validateCollection(T values, Consumer<U> validator) {
//        values.forEach(validator);
//    }
//
//    public static <T> void validateDuplicate(List<T> values, String errorMessage) {
//        Set<T> set = new HashSet<>();
//        values.forEach(value -> {
//            Assert.isTrue(!set.contains(value), errorMessage);
//            set.add(value);
//        });
//    }
//
//    public static <T, U> void validateKeyDuplicate(List<T> values, Function<T, U> getKey, String errorMessage) {
//        Set<U> keySet = new HashSet<>();
//        values.forEach(value -> {
//            U key = getKey.apply(value);
//            Assert.isTrue(!keySet.contains(key), errorMessage);
//            keySet.add(getKey.apply(value));
//        });
//    }
//
//    public static <T> boolean isNotNullOrEmpty(Collection<T> input) {
//        return Objects.nonNull(input) && !input.isEmpty();
//    }
//
//    public static <T> boolean isNullOrEmpty(Collection<T> input) {
//        return Objects.isNull(input) || input.isEmpty();
//    }
//
//    private static void pattern(String input, Pattern pattern, String message) {
//        Assert.isTrue(pattern.matcher(input).matches(), message);
//    }
//
//}