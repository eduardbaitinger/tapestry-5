// Copyright 2007, 2008, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.lang.reflect.Modifier;

/**
 * Ensures that all components implement {@link RenderCommand} by delegating to
 * {@link org.apache.tapestry5.internal.InternalComponentResources#queueRender(org.apache.tapestry5.runtime.RenderQueue)}
 * .
 */
public class RenderCommandWorker implements ComponentClassTransformWorker
{
    private final TransformMethodSignature RENDER_SIGNATURE = new TransformMethodSignature(Modifier.PUBLIC, "void",
            "render", new String[]
            { MarkupWriter.class.getName(), RenderQueue.class.getName() }, null);

    private final ComponentMethodAdvice advice = new ComponentMethodAdvice()
    {
        public void advise(ComponentMethodInvocation invocation)
        {
            InternalComponentResources resources = (InternalComponentResources) invocation.getComponentResources();

            RenderQueue queue = (RenderQueue) invocation.getParameter(1);

            resources.queueRender(queue);
        }
    };

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        // Subclasses don't need to bother, they'll inherit from super-classes.
        if (!transformation.isRootTransformation())
            return;

        transformation.addImplementedInterface(RenderCommand.class);

        transformation.getOrCreateMethod(RENDER_SIGNATURE).addAdvice(advice);
    }
}
