package jp.tonyu.soytext2.search.expr;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;

public class NotExpr extends QueryExpression {
	QueryExpression cond;
	@Override
	public QueryResult matches(DocumentScriptable d) {
		QueryResult res=cond.matches(d);
		return new QueryResult(!res.filterMatched);
	}
	public NotExpr(QueryExpression cond) {
		super();
		this.cond = cond;
	}

}
