/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package security;

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.core.models.Subject;
import models.Persoon;
import play.mvc.Http;
import play.mvc.Result;
import views.html.*;
import controllers.*; // needed for routes

//import views.html.accessFailed;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DefaultDeadboltHandler extends AbstractDeadboltHandler
{
    public Result beforeAuthCheck(Http.Context context)
    {
        // returning null means that everything is OK.  Return a real result if you want a redirect to a login page or
        // somewhere else
        return null;
    }

    public Subject getSubject(Http.Context context)
    {
        return Persoon.findByAccountName(context.session().get("account"));
    }

    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
    {
        // return new MyDynamicResourceHandler();
        return null;
    }

    @Override
    public Result onAuthFailure(Http.Context context,
                                String content)
    {
        // you can return any result from here - forbidden, etc
        //return TODO;
        //return ok(accessFailed.render());
        context.flash().put("error", "Access denied " + content);
        return redirect(routes.Application.login());
    }
}

