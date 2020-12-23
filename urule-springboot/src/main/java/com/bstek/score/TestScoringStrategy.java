package com.bstek.score;

import com.bstek.urule.Utils;
import com.bstek.urule.model.scorecard.runtime.CellItem;
import com.bstek.urule.model.scorecard.runtime.RowItem;
import com.bstek.urule.model.scorecard.runtime.Scorecard;
import com.bstek.urule.model.scorecard.runtime.ScoringStrategy;
import com.bstek.urule.runtime.rete.Context;

import java.math.BigDecimal;

/**
 * 评分卡测试
 *
 * @author wpx
 * @Description 类注释
 * @date 2020/12/23
 */
public class TestScoringStrategy implements ScoringStrategy {
    @Override
    public Object calculate(Scorecard scorecard, Context context) {
        BigDecimal result = new BigDecimal(120.12);
        System.out.println("评分卡名:" + scorecard.getName());
        for (RowItem row : scorecard.getRowItems()) {
            result = result.multiply(Utils.toBigDecimal(row.getScore()));
            for (CellItem cellItem : row.getCellItems()) {
                System.out.println("自定义列" + cellItem.getColName() + ":" + cellItem.getValue());
            }
        }
        return result;
    }
}