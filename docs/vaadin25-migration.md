# Vaadin 25 Migration Progress

## Overview

This document tracks the migration of vaadin-portlet from Vaadin 24.9.10 to Vaadin 25.0.5.

**Target Environment:**
- Vaadin Flow 25.0.5
- Liferay Portal (Jakarta EE 10, Servlet 6.0)
- Java 21+

**Key Constraint:** Liferay targets Jakarta EE 10 (Servlet 6.0), while Vaadin 25 targets Jakarta EE 11 (Servlet 6.1). Servlet 6.1 is backward compatible with 6.0, so this should work as long as no Servlet 6.1-only APIs are used.

---

## Phase 1: Compile Against Flow 25

**Status:** COMPLETED

### Version Changes
- `vaadin.version`: 24.9.10 → 25.0.5
- `flow.version`: 24.9.10 → 25.0.5
- `flow.cdi.version`: 15.2.1 → 16.0.0
- `maven.compiler.source/target`: 17 → 21
- `sonar.java.source`: 17 → 21

### Compilation Errors Found & Fixed

1. **Jackson namespace change** (`PortletWebComponentBootstrapHandler.java:42`)
   - Error: `package com.fasterxml.jackson.databind.node does not exist`
   - Fix: Changed `import com.fasterxml.jackson.databind.node.ObjectNode` to `import tools.jackson.databind.node.ObjectNode`
   - Note: Vaadin 25 uses Jackson 3 with `tools.jackson.*` namespace (NOT `com.fasterxml.jackson.*`)

2. **VaadinResponse interface change** (`VaadinPortletResponse.java:35`, `PortletUidlRequestHandler.java:56`)
   - Error: `does not override abstract method containsHeader(java.lang.String)`
   - Fix: Added `containsHeader(String)` method to both `VaadinPortletResponse` and inner `VaadinResponseWrapper` class
   - Note: New method added to VaadinResponse interface in Vaadin 25

3. **Test compilation: Removed InitParameters constant** (`VaadinPortletContextTest.java:49`, `VaadinPortletConfigTest.java:55`)
   - Error: `cannot find symbol: variable SERVLET_PARAMETER_ENABLE_DEV_SERVER`
   - Fix: Replaced with `SERVLET_PARAMETER_DEVMODE_OPTIMIZE_BUNDLE`
   - Note: `SERVLET_PARAMETER_ENABLE_DEV_SERVER` was removed in Vaadin 25

### Deprecation Warnings (non-breaking)

- `VaadinHttpAndPortletRequest.java` - uses or overrides deprecated API
- `PortletStreamResourceHandler.java` - uses deprecated API marked for removal
- `VaadinPortlet.java` - unchecked or unsafe operations

---

## Phase 2: Elemental → Jackson 3 Conversion

**Status:** COMPLETED (minimal changes needed)

### Findings

The portlet module had minimal elemental.json exposure:

1. **Only one file affected**: `PortletWebComponentBootstrapHandler.java` used `ObjectNode` from Jackson
2. **Fix**: Single import change from `com.fasterxml.jackson` to `tools.jackson` namespace
3. **No elemental.json imports found** - the codebase was already using Jackson/string-based JSON handling

### Modules Compiling Successfully

- `vaadin-portlet` - BUILD SUCCESS
- `vaadin-portlet-cdi` - BUILD SUCCESS

---

## Phase 3: Fix Remaining API Breakage

**Status:** COMPLETED

### Known Breaking Changes Checked

1. ~~`VaadinSession.setConfiguration()` - removed~~ - NOT used in portlet code
2. ~~`VaadinWebSecurity` - removed~~ - NOT used in portlet code
3. ~~`CompressUtil` - moved to internal package~~ - NOT used in portlet code

### Integration Test Fixes

1. **Label component removed** (`LiferayUploadPortletContent.java`, `LiferayUploadIT.java`)
   - Error: `cannot find symbol: class Label` / `cannot find symbol: class LabelElement`
   - Fix: Replaced `Label` with `Span` and `LabelElement` with `SpanElement`
   - Note: `Label` component was removed in Vaadin 25

2. **Missing dependency** (`LiferayPlainIPCPortlet.java`)
   - Error: `package org.apache.commons.io does not exist`
   - Fix: Added `commons-io:commons-io:2.15.1` dependency to `liferay-portlet30/pom.xml`

### Deprecation Warnings to Investigate

These are warnings (not errors) but should be addressed for future compatibility:

1. **VaadinHttpAndPortletRequest** - uses deprecated API
2. **PortletStreamResourceHandler** - uses API marked for removal
3. **VaadinPortlet** - unchecked/unsafe operations

---

## Phase 4: Build Pipeline Verification

**Status:** COMPLETED (code compiles)

### Build Results

All modules compile successfully:
- `vaadin-portlet` - BUILD SUCCESS (51 tests pass)
- `vaadin-portlet-cdi` - BUILD SUCCESS
- `vaadin-portlet-liferay-it-shared` - BUILD SUCCESS
- `liferay-portlet-deployer` - BUILD SUCCESS
- `liferay-tests-generic` - BUILD SUCCESS (compile)
- `liferay-portlet30` - BUILD SUCCESS (compile)

**Note:** Full frontend build requires a Vaadin commercial license (expected for vaadin-portlet product).

---

## Phase 5: Deploy and Test on Liferay

**Status:** PENDING (requires Liferay environment)

### Test Plan

1. Single portlet rendering
2. Multi-portlet on same page
3. IPC (Inter-Portlet Communication)
4. File upload via resource URLs
5. Navigation/routing within portlets

### Servlet 6.0/6.1 Compatibility Notes

Vaadin 25 targets Servlet 6.1 (Jakarta EE 11), but Liferay uses Servlet 6.0 (Jakarta EE 10).
Servlet 6.1 is backward compatible with 6.0. Must verify no Servlet 6.1-only APIs are used.

---

## Files Modified

### Core Module - vaadin-portlet/
1. `pom.xml` - (parent) Version updates (vaadin, flow, flow.cdi, java)
2. `src/main/java/com/vaadin/flow/portal/PortletWebComponentBootstrapHandler.java` - Jackson import fix (line 42)
3. `src/main/java/com/vaadin/flow/portal/VaadinPortletResponse.java` - Added `containsHeader()` method
4. `src/main/java/com/vaadin/flow/portal/PortletUidlRequestHandler.java` - Added `containsHeader()` to inner `VaadinResponseWrapper` class

### Test Files - vaadin-portlet/
5. `src/test/java/com/vaadin/flow/portal/VaadinPortletContextTest.java` - Replaced removed `SERVLET_PARAMETER_ENABLE_DEV_SERVER` constant
6. `src/test/java/com/vaadin/flow/portal/VaadinPortletConfigTest.java` - Replaced removed `SERVLET_PARAMETER_ENABLE_DEV_SERVER` constant

### Integration Tests
7. `vaadin-portlet-liferay-integration-tests/pom.xml` - Updated Java version to 21
8. `vaadin-portlet-liferay-integration-tests/liferay-tests-generic/src/main/java/.../LiferayUploadPortletContent.java` - Replaced `Label` with `Span`
9. `vaadin-portlet-liferay-integration-tests/liferay-tests-generic/src/test/java/.../LiferayUploadIT.java` - Replaced `LabelElement` with `SpanElement`
10. `vaadin-portlet-liferay-integration-tests/liferay-portlet30/pom.xml` - Added missing `commons-io` dependency

---

## Summary

### Migration Success
The vaadin-portlet module has been successfully migrated to Vaadin 25.0.5. All core modules and integration tests compile without errors. Unit tests pass (51 tests).

### Key Findings
1. **Minimal Jackson impact**: Only one import change needed (not the ~70% expected)
2. **VaadinResponse API change**: New `containsHeader()` method required implementation
3. **Label component removed**: Required replacement with `Span` in integration tests
4. **Clean separation**: The portlet module was well-isolated from Flow internals

### Next Steps
1. Deploy to Liferay environment for runtime testing
2. Verify Servlet 6.0 compatibility at runtime
3. Address deprecation warnings before Vaadin 26
4. Consider explicit theme configuration with `@StyleSheet`
