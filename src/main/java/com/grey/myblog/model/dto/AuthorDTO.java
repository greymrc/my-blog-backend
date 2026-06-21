package com.grey.myblog.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 作者信息 DTO
 *
 * @author grey
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 作者ID
     */
    private Long id;

    /**
     * 作者昵称
     */
    private String nickname;

    /**
     * 作者头像
     */
    private String avatar;
}