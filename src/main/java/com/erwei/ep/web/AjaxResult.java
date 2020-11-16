package com.erwei.ep.web;

import com.erwei.ep.api.TransactionException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接口返回类
 * @create 2019-11-26 14:57
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AjaxResult<T> {

    /**
     * 成功的返回码
     */
    public static final Integer CODE_SUCCESS = 0;

    /**
     * 失败的返回码
     */
    public static final Integer CODE_ERROR = -1;

    /**
     * <pre>
     * 字段名：返回码
     * 变量名：code
     * 是否必填：是
     * 示例值：0
     * 描述：成功返回0,失败返回-1
     * </pre>
     */
    protected int code; //编码

    /**
     * <pre>
     * 字段名：详细信息
     * 变量名：msg
     * 是否必填：是
     * 示例值："OK"
     * 描述：成功为"OK",当code不为0时返回错误原因,例如"签名失败"、"参数格式校验错误"
     * </pre>
     */
    protected String msg;

    protected T data; //携带的数据

    public static AjaxResult<?> success(String msg) {
        return success(msg, null);
    }

    public static <T> AjaxResult<T> success(T data) {
        return success("OK", data);
    }

    /**
     * 成功
     *
     * @param msg
     *            信息
     * @param data
     *            数据
     * @param <T>
     *            T
     * @return T
     */
    public static <T> AjaxResult<T> success(String msg, T data) {
        AjaxResult<T> result = new AjaxResult<>();
        result.setCode(CODE_SUCCESS);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 返回默认的系统异常
     */
    public static <T> AjaxResult<T> error() {
        return error(CODE_ERROR, "请求异常,请联系管理员");
    }

    public static <T> AjaxResult<T> error(String msg) {
        return error(CODE_ERROR, msg);
    }

    public static <T> AjaxResult<T> error(TransactionException ex) {
        return error(ex.getCode(), ex.getMessage());
    }

    /**
     * 错误
     *
     * @param code
     *            错误码
     * @param msg
     *            信息
     * @param <T>
     *            T
     * @return AjaxResult
     */
    public static <T> AjaxResult<T> error(int code, String msg) {
        AjaxResult<T> result = new AjaxResult<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }



    public static final int INVALID_SESSION = 50000;
    public static final int SIGN_MONEY_IS_EMPTY = 10000;

    /**
     * 因服务端无用户session,返回无用户Session的异常码: <code>50000</code>
     * @see #INVALID_SESSION
     */
    public static <T> AjaxResult<T> invalidSession() {
        AjaxResult<T> result = new AjaxResult<>();
        result.setCode(INVALID_SESSION);
        result.setMsg("SESSION无效或已过期");
        return result;
    }


    /**
     * 签到金额已经领完,返回异常码: <code>10000</code>
     * @see #SIGN_MONEY_IS_EMPTY
     */
    public static <T> AjaxResult<T> signMoneyIsEmpty() {
        AjaxResult<T> result = new AjaxResult<>();
        result.setCode(SIGN_MONEY_IS_EMPTY);
        result.setMsg("签到红包已领完");
        return result;
    }


    @JsonIgnore
    public boolean isFailed() {
        return (this.getCode() != CODE_SUCCESS);
    }

}
