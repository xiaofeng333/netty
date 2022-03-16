/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;

/**
 * sub-page的Metrics。
 * Metrics for a sub-page.
 */
public interface PoolSubpageMetric {

    /**
     * 返回可以从sub-page分配的最大元素数。
     * Return the number of maximal elements that can be allocated out of the sub-page.
     */
    int maxNumElements();

    /**
     * 返回可被分配的可用元素数量。
     * Return the number of available elements to be allocated.
     */
    int numAvailable();

    /**
     * 返回将被分配的元素大小(byte单位)
     * Return the size (in bytes) of the elements that will be allocated.
     */
    int elementSize();

    /**
     * 返回page size(byte单位)
     * Return the page size (in bytes) of this page.
     */
    int pageSize();
}

