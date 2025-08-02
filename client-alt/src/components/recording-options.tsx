import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Checkbox } from "@/components/ui/checkbox";
import { Slider } from "@/components/ui/slider";
import type { RecordingOptions } from "@shared/schema";

interface RecordingOptionsProps {
  options: RecordingOptions;
  onOptionsChange: (options: RecordingOptions) => void;
  disabled?: boolean;
}

export default function RecordingOptions({
  options,
  onOptionsChange,
  disabled = false,
}: RecordingOptionsProps) {
  const updateOption = <K extends keyof RecordingOptions>(
    key: K,
    value: RecordingOptions[K]
  ) => {
    onOptionsChange({ ...options, [key]: value });
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      {/* Screen Selection */}
      <Card>
        <CardHeader>
          <CardTitle>Screen Selection</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <RadioGroup
            value={options.screenSource}
            onValueChange={(value) => updateOption("screenSource", value as "entire" | "window" | "tab")}
            disabled={disabled}
          >
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="entire" id="entire" />
              <Label htmlFor="entire" className="flex-1 cursor-pointer">
                <div>
                  <p className="font-medium text-gray-900">Entire Screen</p>
                  <p className="text-sm text-gray-500">Capture everything on your display</p>
                </div>
              </Label>
            </div>
            
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="window" id="window" />
              <Label htmlFor="window" className="flex-1 cursor-pointer">
                <div>
                  <p className="font-medium text-gray-900">Application Window</p>
                  <p className="text-sm text-gray-500">Record a specific application</p>
                </div>
              </Label>
            </div>
            
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="tab" id="tab" />
              <Label htmlFor="tab" className="flex-1 cursor-pointer">
                <div>
                  <p className="font-medium text-gray-900">Browser Tab</p>
                  <p className="text-sm text-gray-500">Capture a single browser tab</p>
                </div>
              </Label>
            </div>
          </RadioGroup>
        </CardContent>
      </Card>
      
      {/* Audio Settings */}
      <Card>
        <CardHeader>
          <CardTitle>Audio Settings</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <Label htmlFor="microphone">Include Microphone</Label>
            <Checkbox
              id="microphone"
              checked={options.includeMicrophone}
              onCheckedChange={(checked) => updateOption("includeMicrophone", !!checked)}
              disabled={disabled}
            />
          </div>
          
          <div className="flex items-center justify-between">
            <Label htmlFor="system-audio">Include System Audio</Label>
            <Checkbox
              id="system-audio"
              checked={options.includeSystemAudio}
              onCheckedChange={(checked) => updateOption("includeSystemAudio", !!checked)}
              disabled={disabled}
            />
          </div>
          
          <div className="space-y-2">
            <Label>Microphone Volume</Label>
            <Slider
              value={[options.microphoneVolume]}
              onValueChange={([value]) => updateOption("microphoneVolume", value)}
              max={100}
              step={1}
              disabled={disabled || !options.includeMicrophone}
              className="w-full"
            />
            <div className="text-sm text-gray-500 text-right">
              {options.microphoneVolume}%
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
