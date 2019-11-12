/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.cdi.annotation;

import javax.inject.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

/**
 * The lifecycle of a RouteScoped component is controlled by route navigation.
 * <p>
 * Every RouteScoped bean belongs to one router component owner.
 * It can be a {@link Route @Route}, or a {@link RouterLayout},
 * or a {@link HasErrorParameter HasErrorParameter}.
 * Beans are qualified by {@link RouteScopeOwner @RouteScopeOwner}
 * to link with their owner.
 * <p>
 * Until owner remains active, all beans owned by it remain in the scope.
 * <p>
 * When a RouteScoped bean is a router component,
 * an owner can be any ancestor {@link RouterLayout}, or the bean itself.
 * Omitting the RouteScopeOwner annotation means owner is the bean itself.
 * <p>
 * Injection with this annotation will create a direct reference to the object
 * rather than a proxy.
 * <p>
 * There are some limitations when not using proxies. Circular referencing (that
 * is, injecting A to B and B to A) will not work.
 * Injecting into a larger scope will bind the instance
 * from the currently active smaller scope, and will ignore smaller scope change.
 * For example after being injected into session scope it will point to the same
 * RouteScoped bean instance ( even it is destroyed ) regardless of UI,
 * or any navigation change.
 * <p>
 * The sister annotation to this is the {@link NormalRouteScoped}. Both annotations
 * reference the same underlying scope, so it is possible to get both a proxy
 * and a direct reference to the same object by using different annotations.
 */
@Scope
@Inherited
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD,
        ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface RouteScoped {
}
