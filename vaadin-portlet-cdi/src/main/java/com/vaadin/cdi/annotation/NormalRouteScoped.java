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

import javax.enterprise.context.NormalScope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

/**
 * The lifecycle of a NormalRouteScoped bean is controlled by route navigation.
 * <p>
 * Every NormalRouteScoped bean belongs to one router component owner.
 * It can be a {@link Route @Route}, or a {@link RouterLayout},
 * or a {@link HasErrorParameter HasErrorParameter}.
 * Beans are qualified by {@link RouteScopeOwner @RouteScopeOwner}
 * to link with their owner.
 * <p>
 * Until owner remains active, all beans owned by it remain in the scope.
 * <p>
 * You cannot use this scope with Vaadin Components. Proxy Components do not
 * work correctly within the Vaadin framework, so as a precaution the Vaadin CDI
 * plugin will not deploy if any such beans are discovered.
 * <p>
 * The sister annotation to this is the {@link RouteScoped}. Both annotations
 * reference the same underlying scope, so it is possible to get both a proxy
 * and a direct reference to the same object by using different annotations.
 */
@NormalScope
@Inherited
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD,
        ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface NormalRouteScoped {
}
