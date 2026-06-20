package com.grey.myblog.exception;

import com.grey.myblog.model.enums.ErrorCode;

import java.util.Collection;
import java.util.Objects;

/**
 * 业务断言工具类
 * 用于参数校验和业务条件判断，失败时抛出 BusinessException
 *
 * @author grey
 */
public class AssertUtil {

    private AssertUtil() {
        // 工具类，禁止实例化
    }

    // ==================== 基础断言方法 ====================

    /**
     * 断言条件为true，否则抛出异常
     *
     * @param condition      条件
     * @param runtimeException 自定义运行时异常
     */
    public static void isTrue(boolean condition, RuntimeException runtimeException) {
        if (!condition) {
            throw runtimeException;
        }
    }

    /**
     * 断言条件为true，否则抛出业务异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void isTrue(boolean condition, ErrorCode errorCode) {
        isTrue(condition, new BusinessException(errorCode));
    }

    /**
     * 断言条件为true，否则抛出业务异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void isTrue(boolean condition, ErrorCode errorCode, String message) {
        isTrue(condition, new BusinessException(errorCode, message));
    }

    /**
     * 断言条件为false，否则抛出异常
     *
     * @param condition      条件
     * @param runtimeException 自定义运行时异常
     */
    public static void isFalse(boolean condition, RuntimeException runtimeException) {
        isTrue(!condition, runtimeException);
    }

    /**
     * 断言条件为false，否则抛出业务异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void isFalse(boolean condition, ErrorCode errorCode) {
        isTrue(!condition, errorCode);
    }

    /**
     * 断言条件为false，否则抛出业务异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void isFalse(boolean condition, ErrorCode errorCode, String message) {
        isTrue(!condition, errorCode, message);
    }

    // ==================== 空值断言 ====================

    /**
     * 断言对象不为null，否则抛出异常
     *
     * @param object         待检查对象
     * @param runtimeException 自定义运行时异常
     */
    public static void notNull(Object object, RuntimeException runtimeException) {
        isTrue(object != null, runtimeException);
    }

    /**
     * 断言对象不为null，否则抛出业务异常
     *
     * @param object    待检查对象
     * @param errorCode 错误码
     */
    public static void notNull(Object object, ErrorCode errorCode) {
        isTrue(object != null, errorCode);
    }

    /**
     * 断言对象不为null，否则抛出业务异常
     *
     * @param object    待检查对象
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void notNull(Object object, ErrorCode errorCode, String message) {
        isTrue(object != null, errorCode, message);
    }

    /**
     * 断言对象为null，否则抛出异常
     *
     * @param object         待检查对象
     * @param runtimeException 自定义运行时异常
     */
    public static void isNull(Object object, RuntimeException runtimeException) {
        isTrue(object == null, runtimeException);
    }

    /**
     * 断言对象为null，否则抛出业务异常
     *
     * @param object    待检查对象
     * @param errorCode 错误码
     */
    public static void isNull(Object object, ErrorCode errorCode) {
        isTrue(object == null, errorCode);
    }

    /**
     * 断言对象为null，否则抛出业务异常
     *
     * @param object    待检查对象
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void isNull(Object object, ErrorCode errorCode, String message) {
        isTrue(object == null, errorCode, message);
    }

    // ==================== 字符串断言 ====================

    /**
     * 断言字符串不为空（不为null且长度大于0），否则抛出异常
     *
     * @param str            待检查字符串
     * @param runtimeException 自定义运行时异常
     */
    public static void notEmpty(String str, RuntimeException runtimeException) {
        isTrue(str != null && !str.isEmpty(), runtimeException);
    }

    /**
     * 断言字符串不为空，否则抛出业务异常
     *
     * @param str       待检查字符串
     * @param errorCode 错误码
     */
    public static void notEmpty(String str, ErrorCode errorCode) {
        isTrue(str != null && !str.isEmpty(), errorCode);
    }

    /**
     * 断言字符串不为空，否则抛出业务异常
     *
     * @param str       待检查字符串
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void notEmpty(String str, ErrorCode errorCode, String message) {
        isTrue(str != null && !str.isEmpty(), errorCode, message);
    }

    /**
     * 断言字符串不为空白（不为null且包含至少一个非空白字符），否则抛出异常
     *
     * @param str            待检查字符串
     * @param runtimeException 自定义运行时异常
     */
    public static void notBlank(String str, RuntimeException runtimeException) {
        isTrue(str != null && !str.isBlank(), runtimeException);
    }

    /**
     * 断言字符串不为空白，否则抛出业务异常
     *
     * @param str       待检查字符串
     * @param errorCode 错误码
     */
    public static void notBlank(String str, ErrorCode errorCode) {
        isTrue(str != null && !str.isBlank(), errorCode);
    }

    /**
     * 断言字符串不为空白，否则抛出业务异常
     *
     * @param str       待检查字符串
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void notBlank(String str, ErrorCode errorCode, String message) {
        isTrue(str != null && !str.isBlank(), errorCode, message);
    }

    // ==================== 集合断言 ====================

    /**
     * 断言集合不为空（不为null且包含元素），否则抛出异常
     *
     * @param collection     待检查集合
     * @param runtimeException 自定义运行时异常
     */
    public static void notEmpty(Collection<?> collection, RuntimeException runtimeException) {
        isTrue(collection != null && !collection.isEmpty(), runtimeException);
    }

    /**
     * 断言集合不为空，否则抛出业务异常
     *
     * @param collection 待检查集合
     * @param errorCode  错误码
     */
    public static void notEmpty(Collection<?> collection, ErrorCode errorCode) {
        isTrue(collection != null && !collection.isEmpty(), errorCode);
    }

    /**
     * 断言集合不为空，否则抛出业务异常
     *
     * @param collection 待检查集合
     * @param errorCode  错误码
     * @param message    错误信息
     */
    public static void notEmpty(Collection<?> collection, ErrorCode errorCode, String message) {
        isTrue(collection != null && !collection.isEmpty(), errorCode, message);
    }

    /**
     * 断言数组不为空（不为null且长度大于0），否则抛出异常
     *
     * @param array          待检查数组
     * @param runtimeException 自定义运行时异常
     */
    public static void notEmpty(Object[] array, RuntimeException runtimeException) {
        isTrue(array != null && array.length > 0, runtimeException);
    }

    /**
     * 断言数组不为空，否则抛出业务异常
     *
     * @param array     待检查数组
     * @param errorCode 错误码
     */
    public static void notEmpty(Object[] array, ErrorCode errorCode) {
        isTrue(array != null && array.length > 0, errorCode);
    }

    /**
     * 断言数组不为空，否则抛出业务异常
     *
     * @param array     待检查数组
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void notEmpty(Object[] array, ErrorCode errorCode, String message) {
        isTrue(array != null && array.length > 0, errorCode, message);
    }

    // ==================== 数值断言 ====================

    /**
     * 断言数值大于指定值，否则抛出异常
     *
     * @param value          待检查数值
     * @param target         目标值
     * @param runtimeException 自定义运行时异常
     */
    public static void isGreaterThan(int value, int target, RuntimeException runtimeException) {
        isTrue(value > target, runtimeException);
    }

    /**
     * 断言数值大于指定值，否则抛出业务异常
     *
     * @param value     待检查数值
     * @param target    目标值
     * @param errorCode 错误码
     */
    public static void isGreaterThan(int value, int target, ErrorCode errorCode) {
        isTrue(value > target, errorCode);
    }

    /**
     * 断言数值大于指定值，否则抛出业务异常
     *
     * @param value     待检查数值
     * @param target    目标值
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void isGreaterThan(int value, int target, ErrorCode errorCode, String message) {
        isTrue(value > target, errorCode, message);
    }

    /**
     * 断言数值大于等于指定值，否则抛出异常
     *
     * @param value          待检查数值
     * @param target         目标值
     * @param runtimeException 自定义运行时异常
     */
    public static void isGreaterOrEqual(int value, int target, RuntimeException runtimeException) {
        isTrue(value >= target, runtimeException);
    }

    /**
     * 断言数值大于等于指定值，否则抛出业务异常
     *
     * @param value     待检查数值
     * @param target    目标值
     * @param errorCode 错误码
     */
    public static void isGreaterOrEqual(int value, int target, ErrorCode errorCode) {
        isTrue(value >= target, errorCode);
    }

    /**
     * 断言数值为正数（大于0），否则抛出异常
     *
     * @param value          待检查数值
     * @param runtimeException 自定义运行时异常
     */
    public static void isPositive(int value, RuntimeException runtimeException) {
        isTrue(value > 0, runtimeException);
    }

    /**
     * 断言数值为正数，否则抛出业务异常
     *
     * @param value     待检查数值
     * @param errorCode 错误码
     */
    public static void isPositive(int value, ErrorCode errorCode) {
        isTrue(value > 0, errorCode);
    }

    /**
     * 断言数值为正数，否则抛出业务异常
     *
     * @param value     待检查数值
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void isPositive(int value, ErrorCode errorCode, String message) {
        isTrue(value > 0, errorCode, message);
    }

    // ==================== 对象相等断言 ====================

    /**
     * 断言两个对象相等，否则抛出异常
     *
     * @param obj1           对象1
     * @param obj2           对象2
     * @param runtimeException 自定义运行时异常
     */
    public static void equals(Object obj1, Object obj2, RuntimeException runtimeException) {
        isTrue(Objects.equals(obj1, obj2), runtimeException);
    }

    /**
     * 断言两个对象相等，否则抛出业务异常
     *
     * @param obj1      对象1
     * @param obj2      对象2
     * @param errorCode 错误码
     */
    public static void equals(Object obj1, Object obj2, ErrorCode errorCode) {
        isTrue(Objects.equals(obj1, obj2), errorCode);
    }

    /**
     * 断言两个对象相等，否则抛出业务异常
     *
     * @param obj1      对象1
     * @param obj2      对象2
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void equals(Object obj1, Object obj2, ErrorCode errorCode, String message) {
        isTrue(Objects.equals(obj1, obj2), errorCode, message);
    }

    /**
     * 断言两个对象不相等，否则抛出异常
     *
     * @param obj1           对象1
     * @param obj2           对象2
     * @param runtimeException 自定义运行时异常
     */
    public static void notEquals(Object obj1, Object obj2, RuntimeException runtimeException) {
        isTrue(!Objects.equals(obj1, obj2), runtimeException);
    }

    /**
     * 断言两个对象不相等，否则抛出业务异常
     *
     * @param obj1      对象1
     * @param obj2      对象2
     * @param errorCode 错误码
     */
    public static void notEquals(Object obj1, Object obj2, ErrorCode errorCode) {
        isTrue(!Objects.equals(obj1, obj2), errorCode);
    }

    // ==================== 状态断言 ====================

    /**
     * 断言状态有效，否则抛出异常
     * 常用于检查对象的状态是否满足业务要求
     *
     * @param expression     状态表达式
     * @param runtimeException 自定义运行时异常
     */
    public static void state(boolean expression, RuntimeException runtimeException) {
        isTrue(expression, runtimeException);
    }

    /**
     * 断言状态有效，否则抛出业务异常
     *
     * @param expression 状态表达式
     * @param errorCode  错误码
     */
    public static void state(boolean expression, ErrorCode errorCode) {
        isTrue(expression, errorCode);
    }

    /**
     * 断言状态有效，否则抛出业务异常
     *
     * @param expression 状态表达式
     * @param errorCode  错误码
     * @param message    错误信息
     */
    public static void state(boolean expression, ErrorCode errorCode, String message) {
        isTrue(expression, errorCode, message);
    }
}
