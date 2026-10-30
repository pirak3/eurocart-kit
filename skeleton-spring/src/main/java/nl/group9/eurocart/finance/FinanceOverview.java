package nl.group9.eurocart.finance;

import java.util.List;

/** All orders, newest first, and the totals of the paid ones per currency. */
public record FinanceOverview(List<FinanceOrder> orders, List<CurrencyTotal> paidTotals) {
}
