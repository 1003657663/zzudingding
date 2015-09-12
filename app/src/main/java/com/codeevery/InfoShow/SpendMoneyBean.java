package com.codeevery.InfoShow;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by songchao on 15/8/18.
 */
public class SpendMoneyBean {
    public int totalProperty;
    public List<RootBean> root;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String enter = "\r\n";
        sb.append(totalProperty+enter);
        sb.append(root.get(0).DSCRP+enter);
        sb.append(root.get(7).OPFARE+enter);
        return sb.toString();
    }
}
