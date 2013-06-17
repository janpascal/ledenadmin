package utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class FormatHelper {

    public static String formatCurrency(BigDecimal value, String format) {
        DecimalFormat formatter = new DecimalFormat(format);
        return formatter.format(value.doubleValue());
   }

   public static String formatCurrency(BigDecimal value) {
        return formatCurrency(value, "0.00");
   }

}
