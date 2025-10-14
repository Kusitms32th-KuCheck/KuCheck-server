package onku.backend.domain.member.enums

enum class Role {
    GUEST, USER, ADMIN, MANAGEMENT;

    fun authorities(): List<String> = when (this) {
        GUEST -> listOf("GUEST")
        USER -> listOf("USER")
        ADMIN -> listOf("ADMIN","USER")
        MANAGEMENT -> listOf("MANAGEMENT","ADMIN","USER")
    }
}