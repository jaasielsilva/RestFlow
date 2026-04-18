package com.jaasielsilva.erpcorporativo.app.exception;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.jaasielsilva.erpcorporativo.app.dto.api.error.ApiErrorResponse;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GlobalErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    @RequestMapping(value = "/error", produces = "text/html")
    public ModelAndView handleHtmlError(HttpServletRequest request) {
        HttpStatus status = resolveStatus(request);
        Map<String, Object> attributes = getErrorAttributes(request);

        ModelAndView modelAndView = new ModelAndView("error/" + status.value());
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", attributes.getOrDefault("error", status.getReasonPhrase()));
        modelAndView.addObject("message", attributes.getOrDefault("message", "Ocorreu um erro na aplicação."));
        modelAndView.addObject("path", attributes.getOrDefault("path", request.getRequestURI()));
        return modelAndView;
    }

    @RequestMapping("/error")
    public ResponseEntity<ApiErrorResponse> handleApiError(HttpServletRequest request) {
        HttpStatus status = resolveStatus(request);
        Map<String, Object> attributes = getErrorAttributes(request);
        ApiErrorCode code = mapCode(status);

        ApiErrorResponse response = new ApiErrorResponse(
                "error",
                code.name(),
                code.name().toLowerCase(),
                String.valueOf(attributes.getOrDefault("message", status.getReasonPhrase())),
                String.valueOf(attributes.getOrDefault("path", request.getRequestURI())),
                OffsetDateTime.now()
        );

        return ResponseEntity.status(status).body(response);
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);
        return errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
    }

    private HttpStatus resolveStatus(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode instanceof Integer code) {
            return HttpStatus.valueOf(code);
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ApiErrorCode mapCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ApiErrorCode.BAD_REQUEST;
            case UNAUTHORIZED -> ApiErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ApiErrorCode.ACCESS_DENIED;
            case NOT_FOUND -> ApiErrorCode.RESOURCE_NOT_FOUND;
            case METHOD_NOT_ALLOWED -> ApiErrorCode.METHOD_NOT_ALLOWED;
            default -> ApiErrorCode.INTERNAL_SERVER_ERROR;
        };
    }
}
