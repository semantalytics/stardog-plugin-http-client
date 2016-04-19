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

    private static final String SLASH = File.separator;
    private static String downloadsDir;

    static {
        if(System.getenv("STARDOG_HOME") == null) {
            downloadsDir = System.getProperty("user.home") + SLASH + "stardog-downloads";
        } else {
            downloadsDir = System.getenv("STARDOG_HOME") + SLASH + "downloads";
        }
    }

    public Get() {
        super(Range.closed(1, 1), "http://semantalytics.com/2016/04/ns/stardog/udf/httpclient/get");
    }

    private File getDestinationFile(final String hash) {
        return new File(downloadsDir + SLASH + hash.substring(0, 4) + SLASH + hash.substring(4,8) + SLASH + hash.substring(7));
    }

    private Get(final Get get) {
        super(get);
    }

    @Override
    protected Value internalEvaluate(final Value... values) throws ExpressionEvaluationException {

        assertIRI(values[0]);

        final File tempFile;

        try {
            tempFile = File.createTempFile("stardog", ".tmp");
        } catch (IOException e) {
            throw new ExpressionEvaluationException("unable to create temp file", e);
        }

        try {
            Request.Get(values[0].stringValue()).execute().saveContent(tempFile);
        } catch (IOException e) {
            throw new ExpressionEvaluationException("Unable to write to temp file", e);
        }

        final String hash;

        try {
            hash = Files.hash(tempFile, Hashing.sha1()).toString();
        } catch (IOException e) {
            throw new ExpressionEvaluationException("Unable to calculate hash of file", e);
        }

        final File destFile = getDestinationFile(hash);

        try {
            Files.createParentDirs(getDestinationFile(hash));
        } catch(IOException e) {
            throw new ExpressionEvaluationException("Unable to create parent directories to final destination");
        }

        try {
            destFile.createNewFile();
        } catch(IOException e) {
            throw new ExpressionEvaluationException("Unable to create destination file");
        }

        if (!destFile.exists()) {
            try {
                Files.move(tempFile, destFile);
            } catch(IOException e) {
                throw new ExpressionEvaluationException("Unable to copy temp final to destination");
            }
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
