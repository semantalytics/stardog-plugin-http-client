package com.semantalytics.stardog.plan.filter.functions;

import com.complexible.stardog.plan.filter.ExpressionEvaluationException;
import com.complexible.stardog.plan.filter.ExpressionVisitor;
import com.complexible.stardog.plan.filter.functions.AbstractFunction;
import com.complexible.stardog.plan.filter.functions.Function;
import com.complexible.stardog.plan.filter.functions.UserDefinedFunction;
import org.openrdf.model.Value;

public class Delete extends AbstractFunction implements UserDefinedFunction {

    public Delete() {
        super(1, "http://semantalytics.com/2016/04/ns/stardog/udf/httpclient");
    }

    private Delete(final Delete httpClient) {
        super(httpClient);
    }

    @Override
    protected Value internalEvaluate(final Value... values) throws ExpressionEvaluationException {
        return null;
    }

    @Override
    public Function copy() {
        return new Delete(this);
    }

    @Override
    public void accept(final ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
