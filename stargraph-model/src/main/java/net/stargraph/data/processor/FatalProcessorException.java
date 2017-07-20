package net.stargraph.data.processor;

/**
 * Flags an unrecoverable exception.
 */
public final class FatalProcessorException extends ProcessorException {
    public FatalProcessorException(Throwable cause) {
        super("Processor threw an unrecoverable error :/", cause);
    }
}
