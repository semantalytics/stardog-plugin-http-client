package com.semantalytics.stardog.plan.filter.functions;

import com.complexible.common.rdf.model.Values;
import com.complexible.stardog.plan.filter.ExpressionEvaluationException;
import com.complexible.stardog.plan.filter.ExpressionVisitor;
import com.complexible.stardog.plan.filter.functions.AbstractFunction;
import com.complexible.stardog.plan.filter.functions.Function;
import com.complexible.stardog.plan.filter.functions.UserDefinedFunction;
import com.google.common.collect.Range;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.http.client.fluent.Request;
import org.openrdf.model.Value;

import java.io.File;
import java.io.IOException;

public class Get extends AbstractFunction implements UserDefinedFunction {

    public Get() {
        super(Range.closed(1, 1), "http://semantalytics.com/2016/04/ns/stardog/udf/httpclient/get");
    }

    private Get(final Get httpClient) {
        super(httpClient);
    }

    @Override
    protected Value internalEvaluate(final Value... values) throws ExpressionEvaluationException {
        assertIRI(values[0]);

        final File file;
        try {
            file = File.createTempFile("stardog", ".tmp");
        } catch (IOException e) {
            throw new ExpressionEvaluationException("unable to create temp file", e);
        }

        try {
            Request.Get(values[0].stringValue()).execute().saveContent(file);
        } catch (IOException e) {
            throw new ExpressionEvaluationException("Unable to save temp file", e);
        }
        final File destFile;
        try {
            final String hash = Files.hash(file, Hashing.sha1()).toString();
            final String stardogHome = System.getenv("STARDOG_HOME");
            destFile = new File(stardogHome + hash.substring(0, 4) + "/" + hash.substring(4,8) + "/" + hash.substring(7));
            Files.move(file, destFile);
        } catch (IOException e) {
            throw new ExpressionEvaluationException("Unable to calculate hash of file", e);
        }

        return Values.iri(destFile.toURI().toString());
    }

    @Override
    public Function copy() {
        return new Get(this);
    }

    @Override
    public void accept(final ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
