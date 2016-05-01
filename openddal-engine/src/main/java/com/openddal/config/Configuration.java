/*
 * Copyright 2014-2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Created on 2014年3月25日
// $Id$

package com.openddal.config;

import java.util.List;
import java.util.Properties;

import com.openddal.util.New;

/**
 * @author <a href="mailto:jorgie.mail@gmail.com">jorgie li</a>
 */
public class Configuration {
    
    public boolean forceLoadTableMate = true;
    public Properties settings;
    public List<Shard> cluster;
    public DataSourceProvider provider;
    public String defaultShardName;

    public final List<TableRule> tableRules = New.arrayList();

    public final List<TableRule> nodedIndexs = New.arrayList();
    public final List<SequnceConfig> sequnces = New.arrayList();
    
}
