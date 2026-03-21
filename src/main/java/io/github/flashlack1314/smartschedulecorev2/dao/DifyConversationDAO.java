package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.DifyConversationMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DifyConversationDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Dify会话关联表DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class DifyConversationDAO extends ServiceImpl<DifyConversationMapper, DifyConversationDO>
        implements IService<DifyConversationDO> {

    /**
     * 获取用户最新活跃的会话
     *
     * @param userUuid 用户UUID
     * @param userType 用户类型
     * @return 最新的会话记录，如果不存在则返回null
     */
    public DifyConversationDO getLatestConversation(String userUuid, String userType) {
        return this.lambdaQuery()
                .eq(DifyConversationDO::getUserUuid, userUuid)
                .eq(DifyConversationDO::getUserType, userType)
                .orderByDesc(DifyConversationDO::getUpdatedAt)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 根据用户和Dify会话ID查询
     *
     * @param userUuid          用户UUID
     * @param userType          用户类型
     * @param difyConversationId Dify会话ID
     * @return 会话记录，如果不存在则返回null
     */
    public DifyConversationDO getByUserAndDifyConversationId(String userUuid, String userType, String difyConversationId) {
        return this.lambdaQuery()
                .eq(DifyConversationDO::getUserUuid, userUuid)
                .eq(DifyConversationDO::getUserType, userType)
                .eq(DifyConversationDO::getDifyConversationId, difyConversationId)
                .one();
    }
}
