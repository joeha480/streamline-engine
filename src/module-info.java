module org.daisy.streamline.engine {
	exports org.daisy.streamline.engine;

	requires java.logging;
	requires org.daisy.streamline.api;
	provides org.daisy.streamline.api.tasks.TaskSystemFactory
		with org.daisy.streamline.engine.impl.DefaultTaskSystemFactory;
}