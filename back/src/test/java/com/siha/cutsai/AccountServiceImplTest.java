package com.siha.cutsai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.siha.cutsai.auth.mapper.EmailVerificationMapper;
import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.mapper.MemberMapper;
import com.siha.cutsai.member.service.AccountServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

/** AI작성: 계정 설정과 탈퇴의 비밀번호 확인 및 데이터 처리 순서를 확인한다. */
class AccountServiceImplTest {
    private MemberMapper members;
    private RefreshTokenMapper tokens;
    private EmailVerificationMapper verifications;
    private PasswordEncoder encoder;
    private AccountServiceImpl service;

    /** AI작성: 각 테스트에서 사용할 계정 서비스 의존성을 준비한다. */
    @BeforeEach
    void setUp() {
        members = mock(MemberMapper.class);
        tokens = mock(RefreshTokenMapper.class);
        verifications = mock(EmailVerificationMapper.class);
        encoder = mock(PasswordEncoder.class);
        service = new AccountServiceImpl(members, tokens, verifications, encoder);
        when(members.findAccountForUpdate(1L))
                .thenReturn(Optional.of(new MemberMapper.AccountCredentials("writer@example.com", "작가", "stored-hash")));
    }

    /** AI작성: 이미 사용 중인 닉네임은 저장하지 않는다. */
    @Test
    void rejectsDuplicateNickname() {
        when(members.existsByNickname("다른작가")).thenReturn(true);
        ApiException error = assertThrows(ApiException.class, () -> service.changeNickname(1L, "다른작가"));
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("USER-003", error.getCode());
        verify(members, never()).updateNickname(anyLong(), anyString(), any());
    }

    /** AI작성: 앞뒤 공백을 제거한 닉네임을 저장하고 저장된 값을 반환한다. */
    @Test
    void changesNicknameAndReturnsSavedValue() {
        when(members.updateNickname(eq(1L), eq("새작가"), any())).thenReturn(1);

        String result = service.changeNickname(1L, "  새작가  ");

        assertEquals("새작가", result);
        verify(members).existsByNickname("새작가");
        verify(members).updateNickname(eq(1L), eq("새작가"), any());
    }

    /** AI작성: 현재 비밀번호가 틀리면 새 비밀번호를 저장하거나 토큰을 폐기하지 않는다. */
    @Test
    void rejectsPasswordChangeWithWrongCurrentPassword() {
        when(encoder.matches("wrong-password", "stored-hash")).thenReturn(false);
        assertThrows(ApiException.class, () -> service.changePassword(1L, "wrong-password", "newpass1!"));
        verify(members, never()).updatePassword(anyLong(), anyString(), any());
        verifyNoInteractions(tokens);
    }

    /** AI작성: 현재 비밀번호가 맞으면 새 해시 저장 후 기존 토큰을 폐기한다. */
    @Test
    void changesPasswordAndRevokesTokens() {
        when(encoder.matches("current-password", "stored-hash")).thenReturn(true);
        when(encoder.encode("newpass1!")).thenReturn("new-hash");
        when(members.updatePassword(eq(1L), eq("new-hash"), any())).thenReturn(1);
        service.changePassword(1L, "current-password", "newpass1!");
        var order = inOrder(members, tokens);
        order.verify(members).findAccountForUpdate(1L);
        order.verify(members).updatePassword(eq(1L), eq("new-hash"), any());
        order.verify(tokens).revokeAllForMember(eq(1L), any());
    }

    /** AI작성: 현재 비밀번호가 틀리면 탈퇴 관련 데이터를 삭제하지 않는다. */
    @Test
    void rejectsWithdrawalWithWrongCurrentPassword() {
        when(encoder.matches("wrong-password", "stored-hash")).thenReturn(false);
        assertThrows(ApiException.class, () -> service.withdraw(1L, "wrong-password"));
        verify(members, never()).deleteEpisodeSchedules(anyLong());
        verify(members, never()).deleteMember(anyLong());
        verifyNoInteractions(tokens, verifications);
    }

    /** AI작성: 탈퇴가 승인되면 인증 기록과 관련 데이터를 회원보다 먼저 삭제한다. */
    @Test
    void deletesRelatedRecordsBeforeMember() {
        when(encoder.matches("current-password", "stored-hash")).thenReturn(true);
        when(members.deleteMember(1L)).thenReturn(1);
        service.withdraw(1L, "current-password");
        var order = inOrder(verifications, members, tokens);
        order.verify(members).findAccountForUpdate(1L);
        order.verify(verifications).deleteByEmail("writer@example.com");
        order.verify(members).deleteEpisodeSchedules(1L);
        order.verify(tokens).deleteByMemberId(1L);
        order.verify(members).deleteMember(1L);
    }
}
