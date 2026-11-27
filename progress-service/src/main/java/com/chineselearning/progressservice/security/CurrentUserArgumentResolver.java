package com.chineselearning.progressservice.security;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.chineselearning.progressservice.exception.ForbiddenOperationException;

/** Builds a CurrentUser from the identity headers set by the API gateway. */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter)
    {
        return CurrentUser.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
    {
        String userId = webRequest.getHeader("X-User-Id");
        String role = webRequest.getHeader("X-User-Role");

        if (userId == null || role == null)
        {
            throw new ForbiddenOperationException("Missing identity headers");
        }
        try {
            return new CurrentUser(Long.valueOf(userId), role);
        } catch (NumberFormatException e) {
            throw new ForbiddenOperationException("Invalid identity headers");
        }
    }
}