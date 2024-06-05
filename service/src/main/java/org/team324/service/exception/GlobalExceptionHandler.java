package org.team324.service.exception;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.team324.common.BaseErrorCode;
import org.team324.common.ResponseVO;
import org.team324.common.exception.ApplicationException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 处理未知的异常
     * 当系统抛出未知异常时 调用此方法
     * @param e 抛出异常的实例
     * @return
     */
    @ExceptionHandler(value=Exception.class)
    @ResponseBody
    public ResponseVO unknowException(Exception e){
        e.printStackTrace();
        ResponseVO resultBean =new ResponseVO();
        resultBean.setCode(BaseErrorCode.SYSTEM_ERROR.getCode());
        resultBean.setMsg(BaseErrorCode.SYSTEM_ERROR.getError());
        // TODO 未知异常的话，这里写逻辑，发邮件，发短信都可以
        return resultBean;
    }


    /**
     * 处理 Spring Validator 参数校验异常处理
     * 当使用@Valid或@Validated注解进行参数校验失败时，调用此方法
     * @param ex ConstraintViolationException异常实例
     * @return
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    public Object handleMethodArgumentNotValidException(ConstraintViolationException ex) {

        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        ResponseVO resultBean =new ResponseVO();
        resultBean.setCode(BaseErrorCode.PARAMETER_ERROR.getCode());
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            PathImpl pathImpl = (PathImpl) constraintViolation.getPropertyPath();
            // 读取参数字段，constraintViolation.getMessage() 读取验证注解中的message值
            String paramName = pathImpl.getLeafNode().getName();
            String message = "参数{".concat(paramName).concat("}").concat(constraintViolation.getMessage());
            resultBean.setMsg(message);

            return resultBean;
        }
        resultBean.setMsg(BaseErrorCode.PARAMETER_ERROR.getError() + ex.getMessage());
        return resultBean;
    }

    /**
     * 处理自定义的应用程序异常
     * 当应用程序中抛出自定义的异常时，将调用此方法
     * @param e ApplicationException异常实例
     * @return
     */
    @ExceptionHandler(ApplicationException.class)
    @ResponseBody
    public Object applicationExceptionHandler(ApplicationException e) {
        // 使用公共的结果类封装返回结果, 这里我指定状态码为
        ResponseVO resultBean =new ResponseVO();
        resultBean.setCode(e.getCode());
        resultBean.setMsg(e.getError());
        return resultBean;
    }

    /**
     * 处理BindException异常
     * 当使用@ModelAttribute注解的参数绑定失败时，将调用此方法。
     * @param ex BindException异常实例
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public Object  handleException2(BindException ex) {
        FieldError err = ex.getFieldError();
        String message = "参数{".concat(err.getField()).concat("}").concat(err.getDefaultMessage());
        ResponseVO resultBean =new ResponseVO();
        resultBean.setCode(BaseErrorCode.PARAMETER_ERROR.getCode());
        resultBean.setMsg(message);
        return resultBean;


    }

    /**
     * 处理Spring MVC的MethodArgumentNotValidException异常
     * 当方法参数的校验失败时，将调用此方法
     * @param ex MethodArgumentNotValidException异常实例
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public Object  handleException1(MethodArgumentNotValidException ex) {
        StringBuilder errorMsg = new StringBuilder();
        BindingResult re = ex.getBindingResult();
        for (ObjectError error : re.getAllErrors()) {
            errorMsg.append(error.getDefaultMessage()).append(",");
        }
        errorMsg.delete(errorMsg.length() - 1, errorMsg.length());

        ResponseVO resultBean =new ResponseVO();
        resultBean.setCode(BaseErrorCode.PARAMETER_ERROR.getCode());
        resultBean.setMsg(BaseErrorCode.PARAMETER_ERROR.getError() + " : " + errorMsg.toString());
        return resultBean;
    }

}
