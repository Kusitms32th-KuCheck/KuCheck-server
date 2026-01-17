package onku.backend.global.annotation.resolver

import onku.backend.domain.member.Member
import onku.backend.domain.member.service.MemberService
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.auth.jwt.JwtUtil
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthenticatedMemberResolver(
    private val jwtUtil: JwtUtil,
    private val memberService: MemberService
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentMember::class.java) &&
                Member::class.java.isAssignableFrom(parameter.parameterType)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val header = webRequest.getHeader("Authorization") ?: return null
        val token = header.split(" ").getOrNull(1) ?: return null
        val email = jwtUtil.getEmail(token)
        return memberService.getByEmail(email)
    }
}
