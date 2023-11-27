package study.datajpa.entity;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
/*
* @MappedSuperclass는 실제 테이블과 매핑되지 않는다. 단순히 매핑 정보를 상속할 목적으로만 사용된다.
*/
public class JpaBaseEntity {

    @Column(updatable=false)
    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    @PrePersist
    // persist 하기 전에 실행됨
    public void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate
    public void preUpdate(){
        updatedDate = LocalDateTime.now();

    }
}
