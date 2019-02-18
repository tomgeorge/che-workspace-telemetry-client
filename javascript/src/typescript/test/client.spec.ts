/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
'use strict';

import {TelemetryApi, TelemetryClient} from '../src';
import {IBackend, Backend} from './backend';
import moxios from 'moxios';
import axios from 'axios';

describe('RestAPI >', () => {
    let telemetryClient: TelemetryAPI;
    let backend: IBackend;

    beforeEach(() => {
    	telemetryClient = new TelemetryClient();
        backend = new Backend(axios, moxios);

        backend.install();
    });

    afterEach(() => {
        backend.uninstall();
    });

    it('activity test - successful', (done) => {
        backend.stubRequest('POST', '/activity', {
            status: 200,
            responseText: ''
        });

        const spySucceed = jasmine.createSpy('succeed');
        const spyFailed = jasmine.createSpy('failed');
        
        telemetryClient.activity({userId : "userId" }).then(spySucceed, spyFailed);

        backend.wait(() => {
            expect(spySucceed.calls.count()).toEqual(1);
            expect(spyFailed.calls.count()).toEqual(0);
            done();
        });
    });

    it('activity test - Error', (done) => {
        backend.stubRequest('POST', '/activity', {
            status: 500,
            responseText: 'This is an error message !'
        });

        const spySucceed = jasmine.createSpy('succeed');
        const spyFailed = jasmine.createSpy('failed');
        
        telemetryClient.activity({userId : "userId" }).then(spySucceed, spyFailed);

        backend.wait(() => {
            expect(spySucceed.calls.count()).toEqual(0);
            expect(spyFailed.calls.count()).toEqual(1);
            done();
        });
    });
    
});
