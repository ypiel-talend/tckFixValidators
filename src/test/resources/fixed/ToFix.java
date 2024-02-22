/*
 * Copyright (C) 2006-2024 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.jdbc.datastore;

import lombok.Data;
import lombok.ToString;
import org.talend.components.jdbc.common.AuthenticationType;
import org.talend.components.jdbc.common.Driver;
import org.talend.components.jdbc.common.GrantType;
import org.talend.components.jdbc.common.JDBCConfiguration;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.action.Proposable;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.condition.ActiveIfs;
import org.talend.sdk.component.api.configuration.condition.UIScope;
import org.talend.sdk.component.api.configuration.constraint.Min;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.talend.sdk.component.api.configuration.condition.ActiveIfs.Operator.AND;
import static org.talend.sdk.component.api.configuration.condition.ActiveIfs.Operator.OR;

@Data
@GridLayout({
        @GridLayout.Row("first"),
        @GridLayout.Row("second"),
        @GridLayout.Row("third"),
        @GridLayout.Row("fourth"),
        @GridLayout.Row("fifth")
})
@GridLayout(names = GridLayout.FormType.ADVANCED, value = {
        @GridLayout.Row("first"),
        @GridLayout.Row("second"),
        @GridLayout.Row("third"),
        @GridLayout.Row("fourth"),
        @GridLayout.Row("fifth"),
})
@DataStore("ADatastore")
@Checkable("Check")
@Documentation("This is a documentation to fix.")
public class ToFix implements Serializable {

    @Option
    @Documentation("This is the first option.")
    private boolean firstOption;

    @Option
    @ActiveIf(target = "firstOption", value = { "true" })
    @Documentation("This is a second option. There is a dot in the middle.")
    @Proposable("ACTION_NAME")
    private String secondOption;

    @Option
    @ActiveIf(target = "firstOption", value = { "true", "false" })
    @Documentation("This thirs parameter doesn't start by an uppercase. Moreover, it doesn't finish by a dot.")
    @Suggestable(value = "ACTION_NAME", parameters = { "firstOption" })
    private String handler;

    @Option
    @ActiveIf(target = "firstOption", value = { "true" })
    @Documentation("This fourth doesn't have uppercase.")
    @DefaultValue("true")
    private Boolean fourth = true;

    @Option
    @ActiveIf(target = "fourth", value = { "false" })
    @Documentation("This one doesn't end by a dot.")
    private String fifth;

    @Option
    @Documentation("Using $dollard character.")
    private String dollard;

    @Option
    @Documentation("Using \"quotes\" character.")
    private String quotes;

    @Option
    @Documentation("Using \"$both\" characters.")
    private String both;
}
