package com.semantalytics.stardog.plan.filter.functions;

import com.complexible.common.rdf.model.Values;
import com.complexible.stardog.plan.filter.ExpressionEvaluationException;
import com.complexible.stardog.plan.filter.ExpressionVisitor;
import com.complexible.stardog.plan.filter.functions.AbstractFunction;
import com.complexible.stardog.plan.filter.functions.Function;
import com.complexible.stardog.plan.filter.functions.UserDefinedFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.openrdf.model.Value;

import java.io.File;
import java.io.IOException;

public class Delete extends AbstractFunction implements UserDefinedFunction {

    public Delete() {
        super(1, "http://semantalytics.com/2016/04/ns/stardog/udf/httpclient");
    }

    private Delete(final Delete httpClient) {
        super(httpClient);
    }

    @Override
    protected Value internalEvaluate(final Value... values) throws ExpressionEvaluationException {

        assertIRI(values[0]);

        Response response;

        try {
            response = Request.Delete(values[0].stringValue()).execute();
        } catch (IOException e) {
            throw new ExpressionEvaluationException("Unable to write to temp file", e);
        }

        try {
            return Values.literal(response.returnResponse().getStatusLine().getStatusCode());
        } catch (IOException e) {
            throw new ExpressionEvaluationException("Unable to get response");
        }
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
