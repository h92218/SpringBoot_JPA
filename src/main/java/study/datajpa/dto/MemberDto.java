package study.datajpa.dto;

import lombok.Data;

@Data //getter, setter, toString, requiredArgsConstructor 다 포함됨
public class MemberDto {

    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Long id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }
}