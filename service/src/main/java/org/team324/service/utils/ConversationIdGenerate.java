package org.team324.service.utils;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
public class ConversationIdGenerate {


    // A B
    // B A
    // 结果相同
    public static String generateP2PId(String fromId, String toId) {
        int i = fromId.compareTo(toId);
        if (i < 0) {
            return toId + "|" + fromId;
        }else if (i > 0) {
            return fromId + "|" + toId;
        }

        throw new RuntimeException("");
    }
}
