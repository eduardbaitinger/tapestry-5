// Copyright 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.List;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.NonVisual;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.beaneditor.BeanModelImpl;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.PropertyAdapter;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.DataTypeAnalyzer;
import org.apache.tapestry.services.PropertyConduitSource;

public class BeanModelSourceImpl implements BeanModelSource
{
    private final TypeCoercer _typeCoercer;

    private final PropertyAccess _propertyAccess;

    private final PropertyConduitSource _propertyConduitSource;

    private final ClassFactory _classFactory;

    private final DataTypeAnalyzer _dataTypeAnalyzer;

    public BeanModelSourceImpl(TypeCoercer typeCoercer, PropertyAccess propertyAccess,
            PropertyConduitSource propertyConduitSource, ClassFactory classFactory,
            DataTypeAnalyzer dataTypeAnalyzer)
    {
        _typeCoercer = typeCoercer;
        _propertyAccess = propertyAccess;
        _propertyConduitSource = propertyConduitSource;
        _classFactory = classFactory;
        _dataTypeAnalyzer = dataTypeAnalyzer;
    }

    public BeanModel create(Class beanClass, boolean filterReadOnlyProperties,
            ComponentResources resources)
    {
        notNull(beanClass, "beanClass");
        notNull(resources, "resources");

        Messages messages = resources.getMessages();

        ClassPropertyAdapter adapter = _propertyAccess.getAdapter(beanClass);

        BeanModel model = new BeanModelImpl(beanClass, _propertyConduitSource, _typeCoercer,
                messages);

        List<String> propertyNames = newList();

        for (String propertyName : adapter.getPropertyNames())
        {
            PropertyAdapter pa = adapter.getPropertyAdapter(propertyName);

            if (!pa.isRead())
                continue;

            if (pa.getAnnotation(NonVisual.class) != null)
                continue;

            if (filterReadOnlyProperties && !pa.isUpdate())
                continue;

            String dataType = _dataTypeAnalyzer.identifyDataType(pa);

            // If an unregistered type, then ignore the property.

            if (dataType == null)
                continue;

            model.add(propertyName).dataType(dataType);

            propertyNames.add(propertyName);
        }

        // Set default property order for properties that are not explicit.

        List<String> orderedNames = TapestryInternalUtils.orderProperties(
                adapter,
                _classFactory,
                propertyNames);

        for (int i = 0; i < orderedNames.size(); i++)
        {
            String propertyName = orderedNames.get(i);

            model.get(propertyName).order(i);
        }

        return model;
    }
}
