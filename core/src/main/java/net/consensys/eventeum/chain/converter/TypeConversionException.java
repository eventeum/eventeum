package net.consensys.eventeum.chain.converter;

/**
 * An exception occured when converting an object.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class TypeConversionException extends RuntimeException {

    public TypeConversionException(String msg) {
        super(msg);
    }
}
