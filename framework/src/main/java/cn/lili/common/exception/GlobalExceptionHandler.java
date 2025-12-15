package cn.lili.common.exception;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.utils.RequestContextUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.common.vo.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author Chopper
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultMessage<?> handleServiceException(ServiceException e, HttpServletRequest request) {
        ResultCode resultCode = e.getResultCode() != null ? e.getResultCode() : ResultCode.ERROR;
        String message = e.getMsg() != null ? e.getMsg() : resultCode.message();

        logBusinessException(request, resultCode, message);

        return buildResultMessage(resultCode, message);
    }

    /**
     * 处理@Valid校验异常（RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultMessage<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        List<ValidationErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.setFieldErrors(fieldErrors);

        String message = String.format("参数验证失败，共%d个字段错误", fieldErrors.size());

        logValidationException(request, fieldErrors);

        return buildValidationResultMessage(ResultCode.PARAMS_ERROR, message, errorResponse);
    }

    /**
     * 处理表单绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultMessage<ValidationErrorResponse> handleBindException(
            BindException e, HttpServletRequest request) {

        List<ValidationErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.setFieldErrors(fieldErrors);

        String message = String.format("参数绑定失败，共%d个字段错误", fieldErrors.size());

        logValidationException(request, fieldErrors);

        return buildValidationResultMessage(ResultCode.PARAMS_ERROR, message, errorResponse);
    }

    /**
     * 处理方法参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultMessage<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException e, HttpServletRequest request) {

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        List<ValidationErrorResponse.FieldError> fieldErrors = new ArrayList<>();

        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            
            fieldErrors.add(new ValidationErrorResponse.FieldError(
                    fieldName,
                    violation.getInvalidValue(),
                    violation.getMessage()
            ));
        }

        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.setFieldErrors(fieldErrors);

        String message = String.format("参数验证失败，共%d个字段错误", fieldErrors.size());

        logValidationException(request, fieldErrors);

        return buildValidationResultMessage(ResultCode.PARAMS_ERROR, message, errorResponse);
    }

    /**
     * 处理JSON解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultMessage<?> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {

        String message = "请求体格式错误，请检查JSON格式是否正确";

        logWarnException(request, "HttpMessageNotReadableException", message, e);

        return buildResultMessage(ResultCode.PARAMS_ERROR, message);
    }

    /**
     * 处理HTTP方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResultMessage<?> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {

        String message = String.format("不支持%s请求方法", e.getMethod());

        logWarnException(request, "HttpRequestMethodNotSupportedException", message, e);

        return buildResultMessage(ResultCode.ERROR, message);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultMessage<?> handleNoHandlerFound(
            NoHandlerFoundException e, HttpServletRequest request) {

        String message = String.format("接口不存在: %s %s", e.getHttpMethod(), e.getRequestURL());

        logWarnException(request, "NoHandlerFoundException", message, e);

        return buildResultMessage(ResultCode.ERROR, message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultMessage<?> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {

        String message = e.getMessage() != null ? e.getMessage() : "非法参数";

        logWarnException(request, "IllegalArgumentException", message, e);

        return buildResultMessage(ResultCode.PARAMS_ERROR, message);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultMessage<?> handleNullPointerException(
            NullPointerException e, HttpServletRequest request) {

        String message = "系统内部错误，请稍后重试";

        logErrorException(request, "NullPointerException", e);

        return buildResultMessage(ResultCode.ERROR, message);
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultMessage<?> handleGenericException(
            Exception e, HttpServletRequest request) {

        String message = "系统繁忙，请稍后重试";

        logErrorException(request, "Exception", e);

        return buildResultMessage(ResultCode.ERROR, message);
    }

    /**
     * 构建ResultMessage响应
     */
    private ResultMessage<?> buildResultMessage(ResultCode code, String message) {
        ResultMessage<Object> resultMessage = new ResultMessage<>();
        resultMessage.setSuccess(false);
        resultMessage.setCode(code.code());
        resultMessage.setMessage(message);
        resultMessage.setTimestamp(System.currentTimeMillis());
        return resultMessage;
    }

    /**
     * 构建ValidationErrorResponse类型的ResultMessage
     */
    private ResultMessage<ValidationErrorResponse> buildValidationResultMessage(
            ResultCode code, String message, ValidationErrorResponse data) {
        ResultMessage<ValidationErrorResponse> resultMessage = new ResultMessage<>();
        resultMessage.setSuccess(false);
        resultMessage.setCode(code.code());
        resultMessage.setMessage(message);
        resultMessage.setTimestamp(System.currentTimeMillis());
        resultMessage.setResult(data);
        return resultMessage;
    }

    /**
     * 记录业务异常日志
     */
    private void logBusinessException(HttpServletRequest request, ResultCode resultCode, String message) {
        String userId = RequestContextUtil.getCurrentUserId();
        String username = RequestContextUtil.getCurrentUsername();
        String ip = RequestContextUtil.getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String params = RequestContextUtil.getRequestParams(request);

        log.warn("[BUSINESS_EXCEPTION] User[userId={}, username={}] Request[{} {}] IP[{}] Params[{}] ResultCode[{}({})] Message[{}]",
                userId, username, method, uri, ip, params, resultCode.name(), resultCode.code(), message);
    }

    /**
     * 记录参数校验异常日志
     */
    private void logValidationException(HttpServletRequest request, List<ValidationErrorResponse.FieldError> fieldErrors) {
        String userId = RequestContextUtil.getCurrentUserId();
        String username = RequestContextUtil.getCurrentUsername();
        String ip = RequestContextUtil.getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();

        String errorDetails = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getMessage())
                .collect(Collectors.joining("; "));

        log.warn("[VALIDATION_ERROR] User[userId={}, username={}] Request[{} {}] IP[{}] Errors[{}]",
                userId, username, method, uri, ip, errorDetails);
    }

    /**
     * 记录WARN级别异常日志
     */
    private void logWarnException(HttpServletRequest request, String exceptionType, String message, Exception e) {
        String userId = RequestContextUtil.getCurrentUserId();
        String username = RequestContextUtil.getCurrentUsername();
        String ip = RequestContextUtil.getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String params = RequestContextUtil.getRequestParams(request);

        log.warn("[{}] User[userId={}, username={}] Request[{} {}] IP[{}] Params[{}] Message[{}]",
                exceptionType, userId, username, method, uri, ip, params, message);
    }

    /**
     * 记录ERROR级别异常日志（包含堆栈信息）
     */
    private void logErrorException(HttpServletRequest request, String exceptionType, Exception e) {
        String userId = RequestContextUtil.getCurrentUserId();
        String username = RequestContextUtil.getCurrentUsername();
        String ip = RequestContextUtil.getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String params = RequestContextUtil.getRequestParams(request);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        log.error("[SYSTEM_ERROR] User[userId={}, username={}] Request[{} {}] IP[{}] Params[{}] Exception[{}] Message[{}]\nStackTrace:\n{}",
                userId, username, method, uri, ip, params, exceptionType, e.getMessage(), stackTrace);
    }
}
