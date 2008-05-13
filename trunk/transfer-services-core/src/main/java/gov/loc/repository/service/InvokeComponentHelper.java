package gov.loc.repository.service;
import gov.loc.repository.service.annotations.JobType;
import gov.loc.repository.service.annotations.MapParameter;
import gov.loc.repository.service.annotations.Result;
import gov.loc.repository.serviceBroker.ServiceRequest.ObjectEntry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InvokeComponentHelper {
	
	private static final Log log = LogFactory.getLog(InvokeComponentHelper.class);
	private Object component;
	
	private Method jobTypeMethod = null;
	private Method resultMethod = null;
	private Object[] jobTypeParameters;
	private String jobType;
	private Collection<ObjectEntry> objectEntries;
	
	public InvokeComponentHelper(Object component, String jobType, Collection<ObjectEntry> objectEntries) {
		this.component = component;
		this.jobType = jobType;
		this.objectEntries = objectEntries;
	}

	public boolean invoke() throws Exception
	{
		this.findJobTypeMethod();
		if (this.jobTypeMethod == null)
		{
			throw new Exception("Unable to find jobTypeMethod for jobType " + jobType);
		}
		this.findResultMethod();
		this.jobTypeMethod.invoke(this.component, this.jobTypeParameters);
		if (this.resultMethod != null)
		{
			return (Boolean)this.resultMethod.invoke(this.component, (Object[])null); 
		}		
		return true;
	}
	
	public Method getJobTypeMethod()
	{
		return this.jobTypeMethod;
	}
	
	public Method getResultMethod()
	{
		return this.resultMethod;
	}
	
	public Object[] getJobTypeParameters()
	{
		return this.jobTypeParameters;
	}
	
	private void findJobTypeMethod() throws Exception
	{
		for(Method method : component.getClass().getDeclaredMethods())
		{
			log.debug("Trying method " + method.getName());
			//Get super method (which is where the annotations would be located)
			Method superMethod = this.getSuperMethodForJobType(method);
			if (superMethod == null)
			{
				log.debug("Is not annotated for jobType " + this.jobType);
				continue;
			}
			log.debug("Is annotated for jobType " + this.jobType);
			Object[] parameters = new Object[method.getParameterTypes().length];
			//For each parameter
			Annotation[][] parameterAnnotations = superMethod.getParameterAnnotations();
			int satisfiedParameterCount = 0;
			//For each parameter				
			for(int i=0; i < parameterAnnotations.length; i++)
			{
				Annotation[] annotationArray = parameterAnnotations[i];
				if (this.hasSatisfyingValue(annotationArray))
				{
					satisfiedParameterCount++;
				}
			}
			log.debug(MessageFormat.format("{0} of {1} parameters are satisfied", satisfiedParameterCount, parameters.length));
			if (satisfiedParameterCount == parameters.length)
			{
				log.debug("All parameters are satisfied");
				//For each parameter				
				for(int i=0; i < parameterAnnotations.length; i++)
				{
					Annotation[] annotationArray = parameterAnnotations[i];
					parameters[i] = this.getSatisfyingValue(annotationArray);
				}
				this.jobTypeMethod = method;
				this.jobTypeParameters = parameters;
				return;
			}
		}
	}
	
	private void findResultMethod()
	{
		for(Method method : component.getClass().getDeclaredMethods())
		{
			log.debug("Trying method " + method.getName());
			//Get super method (which is where the annotations would be located)
			Method superMethod = this.getSuperMethodForResult(method);
			if (superMethod != null)
			{
				this.resultMethod = method;
			}
			log.debug("Is not annotated for jobType " + this.jobType);			
		}
	}
	
	private Method getSuperMethodForJobType(Method method)
	{
		for(Class<?> clazz : method.getDeclaringClass().getInterfaces())
		{
			log.debug(MessageFormat.format("Checking {0} for method {1} that is annotated with JobType", clazz.getName(), method.getName()));
			Method superMethod;
			try
			{
				superMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
				JobType jobTypeAnnot = (JobType)superMethod.getAnnotation(JobType.class);
				if (jobTypeAnnot != null)
				{
					log.debug("Method contains JobType annotation with value " + jobTypeAnnot.name());
				}
				else
				{
					log.debug("Method does not contain JobType annotation");
				}
				if (jobTypeAnnot != null && this.jobType.equals(jobTypeAnnot.name()))
				{
					return superMethod;
				}
				
			}
			catch(NoSuchMethodException ignore)
			{
				log.debug("Does not contain method");
			}
		}
		return null;
	}

	private Method getSuperMethodForResult(Method method)
	{
		for(Class<?> clazz : method.getDeclaringClass().getInterfaces())
		{
			Method superMethod;
			try
			{
				superMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
				Result resultAnnot = (Result)superMethod.getAnnotation(Result.class);
				if (resultAnnot != null && this.jobType.equals(resultAnnot.jobType()))
				{
					return superMethod;
				}
				
			}
			catch(NoSuchMethodException ignore)
			{
			}
		}
		return null;
	}
	
	
	private boolean hasSatisfyingValue(Annotation[] annotationArray)
	{
		for(Annotation annot : annotationArray)
		{
			if (annot instanceof MapParameter)
			{
				MapParameter mapParameterAnnot = (MapParameter)annot;
				if (this.entriesContain(mapParameterAnnot.name()))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	
	private Collection<ObjectEntry> getObjectEntries(String key)
	{
		Collection<ObjectEntry> entries = new ArrayList<ObjectEntry>();
		for(ObjectEntry entry : this.objectEntries)
		{
			if (entry.getKey().equals(key))
			{
				entries.add(entry);
			}			
		}
		
		return entries;
	}
	
	private boolean entriesContain(String key)
	{
		for(ObjectEntry entry : this.objectEntries)
		{
			if (entry.getKey().equals(key))
			{
				return true;
			}			
		}
		return false;
	}
	
	private Object getSatisfyingValue(Annotation[] annotationArray) throws Exception
	{
		for(Annotation annot : annotationArray)
		{
			if (annot instanceof MapParameter)
			{
				MapParameter mapParameterAnnot = (MapParameter)annot;
				if (this.entriesContain(mapParameterAnnot.name()))
				{
					Collection<ObjectEntry> entries = this.getObjectEntries(mapParameterAnnot.name());
					if (entries.size() != 1)
					{
						throw new Exception("Multiple entries for key " + mapParameterAnnot.name()); 
					}
					Object value = entries.iterator().next().getValueObject();
					log.debug(MessageFormat.format("VariableMap contains value {0} for MapParameter {1}", value, mapParameterAnnot.name()));
					return value;
				}
				else
				{
					log.debug(MessageFormat.format("VariableMap does not contain a value for MapParameter {0}", mapParameterAnnot.name()));
				}
			}
		}
		return null;
	}
	
}
