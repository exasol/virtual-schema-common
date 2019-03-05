package com.exasol.adapter.capabilities;

import static org.hamcrest.collection.IsEmptyIterable.emptyIterableOf;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public final class CapabilityAssertions {
    private CapabilityAssertions() {
        // prevent instantiation
    }

    public static void assertEmptyMainCapabilities(final Capabilities capabilities) {
        assertThat(capabilities.getMainCapabilities(), emptyIterableOf(MainCapability.class));
    }

    public static void assertEmptyLiteralCapabilities(final Capabilities capabilities) {
        assertThat(capabilities.getLiteralCapabilities(), emptyIterableOf(LiteralCapability.class));
    }

    public static void assertEmptyPredicateCapabilities(final Capabilities capabilities) {
        assertThat(capabilities.getPredicateCapabilities(), emptyIterableOf(PredicateCapability.class));
    }

    public static void assertEmptyScalarFunctionCapabilities(final Capabilities capabilities) {
        assertThat(capabilities.getScalarFunctionCapabilities(), emptyIterableOf(ScalarFunctionCapability.class));
    }

    public static void assertEmptyAggregateFunctionCapatilities(final Capabilities capabilities) {
        assertThat(capabilities.getAggregateFunctionCapabilities(), emptyIterableOf(AggregateFunctionCapability.class));
    }

    public static void assertCapabilitesContainAllOf(final Capabilities capabilities,
            final MainCapability... expectedCapabilities) {
        assertThat(capabilities.getMainCapabilities(), containsInAnyOrder(expectedCapabilities));
    }

    public static void assertCapabilitesContainAllOf(final Capabilities capabilities,
            final LiteralCapability... expectedCapabilities) {
        assertThat(capabilities.getLiteralCapabilities(), containsInAnyOrder(expectedCapabilities));
    }

    public static void assertCapabilitesContainAllOf(final Capabilities capabilities,
            final PredicateCapability... expectedCapabilities) {
        assertThat(capabilities.getPredicateCapabilities(), containsInAnyOrder(expectedCapabilities));
    }

    public static void assertCapabilitesContainAllOf(final Capabilities capabilities,
            final ScalarFunctionCapability... expectedCapabilities) {
        assertThat(capabilities.getScalarFunctionCapabilities(), containsInAnyOrder(expectedCapabilities));
    }

    public static void assertCapabilitesContainAllOf(final Capabilities capabilities,
            final AggregateFunctionCapability... expectedCapabilities) {
        assertThat(capabilities.getAggregateFunctionCapabilities(), containsInAnyOrder(expectedCapabilities));
    }

}